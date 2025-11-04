package com.microservices.order.saga;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservices.common.commands.CompleteOrderCommand;
import com.microservices.common.commands.FailOrderCommand;
import com.microservices.common.commands.ProcessPaymentCommand;
import com.microservices.common.commands.RefundPaymentCommand;
import com.microservices.common.commands.ReleaseInventoryCommand;
import com.microservices.common.commands.ReserveInventoryCommand;
import com.microservices.common.commands.ReserveInventoryCommand.InventoryItem;
import com.microservices.common.commands.ValidateUserCommand;
import com.microservices.common.events.InventoryReservationFailedEvent;
import com.microservices.common.events.InventoryReservedEvent;
import com.microservices.common.events.OrderCancelledEvent;
import com.microservices.common.events.OrderCompletedEvent;
import com.microservices.common.events.OrderCreatedEvent;
import com.microservices.common.events.OrderFailedEvent;
import com.microservices.common.events.PaymentFailedEvent;
import com.microservices.common.events.PaymentProcessedEvent;
import com.microservices.common.events.UserValidatedEvent;
import com.microservices.common.events.UserValidationFailedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@Slf4j
public class OrderProcessingSaga {

  // ===== PROCESS STATE ONLY (will be serialized) =====
  private String orderId;
  private Long userId;
  private List<OrderCreatedEvent.OrderItem> orderItems;
  private String transactionId;
  private boolean userValidated = false;
  private boolean inventoryReserved = false;
  private boolean paymentProcessed = false;
  private LocalDateTime startedAt;

  // Track individual inventory reservations
  private Map<Long, Boolean> inventoryReservations = new HashMap<>();

  // ===== INFRASTRUCTURE DEPENDENCIES (will NOT be serialized) =====
  @JsonIgnore
  private transient CommandGateway commandGateway;

  // ===== CONSTRUCTORS =====
  public OrderProcessingSaga() {
    // Default constructor required for deserialization
  }

  // ===== DEPENDENCY INJECTION =====
  @Autowired
  public void setCommandGateway(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  // ===== SAGA EVENT HANDLERS - COORDINATION ONLY =====

  @StartSaga
  @SagaEventHandler(associationProperty = "orderId")
  public void handle(OrderCreatedEvent event) {
    log.info("Starting order processing saga for orderId: {}", event.getOrderId());

    this.orderId = event.getOrderId();
    this.userId = event.getUserId();
    this.orderItems = event.getItems();
    this.startedAt = LocalDateTime.now();

    // Initialize inventory tracking
    for (OrderCreatedEvent.OrderItem item : orderItems) {
      inventoryReservations.put(item.getProductId(), false);
    }

    SagaLifecycle.associateWith("orderId", orderId);

    // ✅ CORRECT - Send command to User aggregate
    commandGateway.send(new ValidateUserCommand(orderId, userId));
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(UserValidatedEvent event) {
    log.info("User validation successful for orderId: {}", event.getOrderId());
    this.userValidated = true;

    // ✅ CORRECT - Send commands to Inventory aggregate for each item
    commandGateway.send(new ReserveInventoryCommand(orderId,
        userId,
        orderItems.stream().map(item -> InventoryItem.builder().productId(item.getProductId())
            .quantity(item.getQuantity()).build()).collect(
            Collectors.toList())));

  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(UserValidationFailedEvent event) {
    log.error("User validation failed for orderId: {}", event.getOrderId());

    // ✅ CORRECT - Send command to Order aggregate to fail it
    commandGateway.send(new FailOrderCommand(
        orderId,
        "User validation failed: " + event.getReason(),
        "SAGA"
    ));
    endSaga();
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(InventoryReservedEvent event) {
    log.info("Inventory reserved for orderId: {}", event.getOrderId());

    // ✅ CORRECT - Send command to Payment aggregate
    commandGateway.send(new ProcessPaymentCommand(
        orderId, // Use orderId as payment identifier
        userId,
        getTotalAmountFromOrderItems(),
        "USD",
        "CREDIT_CARD"
    ));

  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(InventoryReservationFailedEvent event) {
    log.error("Inventory reservation failed for orderId: {}", event.getOrderId());

    // ✅ CORRECT - Send command to Order aggregate
    commandGateway.send(new FailOrderCommand(
        orderId,
        "Inventory reservation failed: " + event.getReason(),
        "SAGA"
    ));
    endSaga();
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(PaymentProcessedEvent event) {
    log.info("Payment processed for orderId: {}", event.getOrderId());
    this.paymentProcessed = true;
    this.transactionId = event.getTransactionId();

    // ✅ CORRECT - Send command to Order aggregate to complete it
    commandGateway.send(new CompleteOrderCommand(orderId, transactionId, null));
    endSaga();
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(PaymentFailedEvent event) {
    log.error("Payment failed for orderId: {}", event.getOrderId());

    // ✅ CORRECT - Compensate by sending commands
    if (inventoryReserved) {

      commandGateway.send(new ReleaseInventoryCommand(
          orderId,
          "because failed"
      ));

    }

    // Fail the order
    commandGateway.send(new FailOrderCommand(
        orderId,
        "Payment failed: " + event.getReason(),
        "SAGA"
    ));
    endSaga();
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(OrderCancelledEvent event) {
    log.info("Order cancelled during saga processing for orderId: {}", event.getOrderId());

    // ✅ CORRECT - Compensate via commands
    if (inventoryReserved) {
      commandGateway.send(new ReleaseInventoryCommand(
          orderId,
          "Order cancelled"));
    }

    if (paymentProcessed) {
      commandGateway.send(new RefundPaymentCommand(
          orderId,
          transactionId,
          "Order cancelled during processing"
      ));
    }

    endSaga();
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(OrderCompletedEvent event) {
    log.info("Order completed successfully for orderId: {}", event.getOrderId());
    // Saga ends naturally - no @EndSaga needed here as it's handled by the event
  }

  @SagaEventHandler(associationProperty = "orderId")
  public void handle(OrderFailedEvent event) {
    log.info("Order failed for orderId: {}", event.getOrderId());
    // Saga ends naturally - no @EndSaga needed here as it's handled by the event
  }

  // ===== HELPER METHODS =====

  private boolean isAllInventoryReserved() {
    return inventoryReservations.values().stream().allMatch(Boolean::booleanValue);
  }

  private BigDecimal getTotalAmountFromOrderItems() {
    // Simple calculation from stored order data - no business logic
    return orderItems.stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @EndSaga
  public void endSaga() {
    log.info("Ending order processing saga for orderId: {}", orderId);
    SagaLifecycle.end();
  }

  // ===== GETTERS AND SETTERS (only for serializable business state) =====

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public List<OrderCreatedEvent.OrderItem> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(List<OrderCreatedEvent.OrderItem> orderItems) {
    this.orderItems = orderItems;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public boolean isUserValidated() {
    return userValidated;
  }

  public void setUserValidated(boolean userValidated) {
    this.userValidated = userValidated;
  }

  public boolean isInventoryReserved() {
    return inventoryReserved;
  }

  public void setInventoryReserved(boolean inventoryReserved) {
    this.inventoryReserved = inventoryReserved;
  }

  public boolean isPaymentProcessed() {
    return paymentProcessed;
  }

  public void setPaymentProcessed(boolean paymentProcessed) {
    this.paymentProcessed = paymentProcessed;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public Map<Long, Boolean> getInventoryReservations() {
    return inventoryReservations;
  }

  public void setInventoryReservations(Map<Long, Boolean> inventoryReservations) {
    this.inventoryReservations = inventoryReservations;
  }
}