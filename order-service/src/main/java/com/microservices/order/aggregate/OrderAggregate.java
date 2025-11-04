package com.microservices.order.aggregate;

import com.microservices.common.commands.CancelOrderCommand;
import com.microservices.common.commands.CompleteOrderCommand;
import com.microservices.common.commands.ConfirmOrderCommand;
import com.microservices.common.commands.CreateOrderCommand;
import com.microservices.common.commands.FailOrderCommand;
import com.microservices.common.commands.RefundOrderCommand;
import com.microservices.common.commands.UpdateOrderItemsCommand;
import com.microservices.common.commands.UpdateOrderStatusCommand;
import com.microservices.common.events.OrderCancelledEvent;
import com.microservices.common.events.OrderCompletedEvent;
import com.microservices.common.events.OrderConfirmedEvent;
import com.microservices.common.events.OrderCreatedEvent;
import com.microservices.common.events.OrderFailedEvent;
import com.microservices.common.events.OrderItemsUpdatedEvent;
import com.microservices.common.events.OrderRefundedEvent;
import com.microservices.common.events.OrderStatusUpdatedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@NoArgsConstructor
@Slf4j
public class OrderAggregate {

  @AggregateIdentifier
  private String orderId;
  private Long userId;
  private List<OrderCreatedEvent.OrderItem> items;
  private BigDecimal totalAmount;
  private String currency;
  private String paymentMethod;
  private String status;
  private String transactionId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // ===== COMMAND HANDLERS - BUSINESS LOGIC =====

  @CommandHandler
  public OrderAggregate(CreateOrderCommand command) {
    log.info("Creating order aggregate for orderId: {}, userId: {}",
        command.getOrderId(), command.getUserId());

    // ✅ Business validation
    if (command.getItems() == null || command.getItems().isEmpty()) {
      throw new IllegalArgumentException("Order must contain at least one item");
    }

    if (command.getUserId() == null) {
      throw new IllegalArgumentException("User ID is required");
    }

    // ✅ Business logic - Calculate total amount
    BigDecimal total = command.getItems().stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (total.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Order total must be positive");
    }

    // Convert to event items
    List<OrderCreatedEvent.OrderItem> eventItems = command.getItems().stream()
        .map(item -> new OrderCreatedEvent.OrderItem(
            item.getProductId(),
            item.getQuantity(),
            item.getPrice()))
        .collect(Collectors.toList());

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderCreatedEvent(
        command.getOrderId(),
        command.getUserId(),
        eventItems,
        total,
        command.getCurrency(),
        command.getPaymentMethod(),
        LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(FailOrderCommand command) {
    log.info("Handling fail order command for orderId: {}, reason: {}",
        command.getOrderId(), command.getReason());

    // ✅ Business validation
    if ("FAILED".equals(this.status)) {
      throw new IllegalStateException("Order is already failed");
    }

    if ("COMPLETED".equals(this.status)) {
      throw new IllegalStateException("Cannot fail a completed order");
    }

    if ("CANCELLED".equals(this.status)) {
      throw new IllegalStateException("Cannot fail a cancelled order");
    }

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderFailedEvent(
        command.getOrderId(),
        command.getReason(),
        command.getFailedBy(),
        LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(CancelOrderCommand command) {
    log.info("Handling cancel order command for orderId: {}", command.getOrderId());

    // ✅ Business validation
    if ("COMPLETED".equals(this.status)) {
      throw new IllegalStateException("Cannot cancel a completed order");
    }

    if ("CANCELLED".equals(this.status)) {
      throw new IllegalStateException("Order is already cancelled");
    }

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderCancelledEvent(
        command.getOrderId(),
        command.getReason(),
        command.getCancelledBy(),
        LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(CompleteOrderCommand command) {
    log.info("Handling complete order command for orderId: {}", command.getOrderId());

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderCompletedEvent(
        command.getOrderId(),
        this.userId,
        this.totalAmount,
        this.transactionId,
        command.getDeliveryDate() != null ? command.getDeliveryDate() : LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(ConfirmOrderCommand command) {
    log.info("Handling confirm order command for orderId: {}", command.getOrderId());

    // ✅ Business validation
    if (!"CREATED".equals(this.status) && !"PROCESSING".equals(this.status)) {
      throw new IllegalStateException(
          "Can only confirm orders that are in CREATED or PROCESSING status");
    }

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderConfirmedEvent(
        command.getOrderId(),
        command.getTransactionId(),
        command.getConfirmedBy(),
        LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(UpdateOrderStatusCommand command) {
    log.info("Handling update order status command for orderId: {}, newStatus: {}",
        command.getOrderId(), command.getNewStatus());

    // ✅ Business validation
    if (this.status.equals(command.getNewStatus())) {
      throw new IllegalStateException("Order is already in status: " + command.getNewStatus());
    }

    if (!isValidStatusTransition(this.status, command.getNewStatus())) {
      throw new IllegalStateException(
          "Invalid status transition from " + this.status + " to " + command.getNewStatus());
    }

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderStatusUpdatedEvent(
        command.getOrderId(),
        this.status,
        command.getNewStatus(),
        command.getReason(),
        command.getUpdatedBy(),
        LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(RefundOrderCommand command) {
    log.info("Handling refund order command for orderId: {}, amount: {}",
        command.getOrderId(), command.getRefundAmount());

    // ✅ Business validation
    if (!"COMPLETED".equals(this.status) && !"CONFIRMED".equals(this.status)) {
      throw new IllegalStateException("Can only refund completed or confirmed orders");
    }

    if (command.getRefundAmount().compareTo(this.totalAmount) > 0) {
      throw new IllegalArgumentException("Refund amount cannot exceed order total");
    }

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderRefundedEvent(
        command.getOrderId(),
        command.getRefundAmount(),
        command.getReason(),
        command.getProcessedBy(),
        LocalDateTime.now()
    ));
  }

  @CommandHandler
  public void handle(UpdateOrderItemsCommand command) {
    log.info("Handling update order items command for orderId: {}", command.getOrderId());

    // ✅ Business validation
    if (!"CREATED".equals(this.status) && !"PROCESSING".equals(this.status)) {
      throw new IllegalStateException(
          "Can only update items for orders in CREATED or PROCESSING status");
    }

    // ✅ Business logic - Calculate new total
    BigDecimal newTotal = calculateNewTotalAmount(command.getItemUpdates());

    // Convert updates to event changes
    List<OrderItemsUpdatedEvent.OrderItemChange> itemChanges = command.getItemUpdates().stream()
        .map(update -> {
          var existingItem = this.items.stream()
              .filter(item -> item.getProductId().equals(update.getProductId()))
              .findFirst();

          return new OrderItemsUpdatedEvent.OrderItemChange(
              update.getProductId(),
              existingItem.map(OrderCreatedEvent.OrderItem::getQuantity).orElse(0),
              update.getNewQuantity(),
              existingItem.map(OrderCreatedEvent.OrderItem::getPrice).orElse(BigDecimal.ZERO),
              update.getNewPrice(),
              update.getAction()
          );
        })
        .collect(Collectors.toList());

    // ✅ Apply business decision
    AggregateLifecycle.apply(new OrderItemsUpdatedEvent(
        command.getOrderId(),
        itemChanges,
        newTotal,
        command.getUpdatedBy(),
        LocalDateTime.now()
    ));
  }

  // ===== EVENT SOURCING HANDLERS - STATE UPDATES ONLY =====

  @EventSourcingHandler
  public void on(OrderCreatedEvent event) {
    log.info("Applying OrderCreatedEvent for orderId: {}", event.getOrderId());
    this.orderId = event.getOrderId();
    this.userId = event.getUserId();
    this.items = event.getItems();
    this.totalAmount = event.getTotalAmount();
    this.currency = event.getCurrency();
    this.paymentMethod = event.getPaymentMethod();
    this.status = "CREATED";
    this.createdAt = event.getCreatedAt();
    this.updatedAt = event.getCreatedAt();
  }

  @EventSourcingHandler
  public void on(OrderFailedEvent event) {
    log.info("Applying OrderFailedEvent for orderId: {}, reason: {}",
        event.getOrderId(), event.getReason());
    this.status = "FAILED";
    this.updatedAt = event.getFailedAt();
  }

  @EventSourcingHandler
  public void on(OrderCancelledEvent event) {
    log.info("Applying OrderCancelledEvent for orderId: {}", event.getOrderId());
    this.status = "CANCELLED";
    this.updatedAt = event.getCancelledAt();
  }

  @EventSourcingHandler
  public void on(OrderCompletedEvent event) {
    log.info("Applying OrderCompletedEvent for orderId: {}", event.getOrderId());
    this.status = "COMPLETED";
    this.updatedAt = event.getCompletedAt();
  }

  @EventSourcingHandler
  public void on(OrderConfirmedEvent event) {
    log.info("Applying OrderConfirmedEvent for orderId: {}", event.getOrderId());
    this.status = "CONFIRMED";
    this.transactionId = event.getTransactionId();
    this.updatedAt = event.getConfirmedAt();
  }

  @EventSourcingHandler
  public void on(OrderStatusUpdatedEvent event) {
    log.info("Applying OrderStatusUpdatedEvent for orderId: {}, newStatus: {}",
        event.getOrderId(), event.getNewStatus());
    this.status = event.getNewStatus();
    this.updatedAt = event.getUpdatedAt();
  }

  @EventSourcingHandler
  public void on(OrderRefundedEvent event) {
    log.info("Applying OrderRefundedEvent for orderId: {}, refundAmount: {}",
        event.getOrderId(), event.getRefundAmount());
    this.status = "REFUNDED";
    this.updatedAt = event.getRefundedAt();
  }

  @EventSourcingHandler
  public void on(OrderItemsUpdatedEvent event) {
    log.info("Applying OrderItemsUpdatedEvent for orderId: {}", event.getOrderId());

    // Update items based on changes
    for (OrderItemsUpdatedEvent.OrderItemChange change : event.getItemChanges()) {
      switch (change.getAction()) {
        case "ADD":
          this.items.add(new OrderCreatedEvent.OrderItem(
              change.getProductId(),
              change.getNewQuantity(),
              change.getNewPrice()));
          break;
        case "UPDATE":
          this.items.stream()
              .filter(item -> item.getProductId().equals(change.getProductId()))
              .findFirst()
              .ifPresent(item -> {
                item.setQuantity(change.getNewQuantity());
                item.setPrice(change.getNewPrice());
              });
          break;
        case "REMOVE":
          this.items.removeIf(item -> item.getProductId().equals(change.getProductId()));
          break;
      }
    }

    this.totalAmount = event.getNewTotalAmount();
    this.updatedAt = event.getUpdatedAt();
  }

  // ===== HELPER METHODS =====

  private boolean isValidStatusTransition(String fromStatus, String toStatus) {
    switch (fromStatus) {
      case "CREATED":
        return List.of("PROCESSING", "CANCELLED", "FAILED").contains(toStatus);
      case "PROCESSING":
        return List.of("CONFIRMED", "FAILED", "CANCELLED").contains(toStatus);
      case "CONFIRMED":
        return List.of("COMPLETED", "REFUNDED", "CANCELLED", "FAILED").contains(toStatus);
      case "COMPLETED":
        return List.of("REFUNDED").contains(toStatus);
      case "FAILED":
        return List.of("PROCESSING", "CANCELLED").contains(toStatus);
      default:
        return false;
    }
  }

  private BigDecimal calculateNewTotalAmount(
      List<UpdateOrderItemsCommand.OrderItemUpdate> updates) {
    List<OrderCreatedEvent.OrderItem> updatedItems = this.items.stream()
        .map(item -> new OrderCreatedEvent.OrderItem(item.getProductId(), item.getQuantity(),
            item.getPrice()))
        .collect(Collectors.toList());

    for (UpdateOrderItemsCommand.OrderItemUpdate update : updates) {
      switch (update.getAction()) {
        case "ADD":
          updatedItems.add(new OrderCreatedEvent.OrderItem(
              update.getProductId(),
              update.getNewQuantity(),
              update.getNewPrice()));
          break;
        case "UPDATE":
          updatedItems.stream()
              .filter(item -> item.getProductId().equals(update.getProductId()))
              .findFirst()
              .ifPresent(item -> {
                item.setQuantity(update.getNewQuantity());
                item.setPrice(update.getNewPrice());
              });
          break;
        case "REMOVE":
          updatedItems.removeIf(item -> item.getProductId().equals(update.getProductId()));
          break;
      }
    }

    return updatedItems.stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}