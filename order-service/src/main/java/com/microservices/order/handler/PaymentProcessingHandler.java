package com.microservices.order.handler;

import com.microservices.common.commands.ProcessPaymentCommand;
import com.microservices.common.events.PaymentFailedEvent;
import com.microservices.common.events.PaymentProcessedEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingHandler {

  private final EventGateway eventGateway;

  @CommandHandler
  public void handle(ProcessPaymentCommand command) {
    log.info("Processing payment for orderId: {}, userId: {}, amount: {}",
        command.getOrderId(), command.getUserId(), command.getAmount());

    try {
      // ✅ Basic validation
      if (command.getAmount() == null
          || command.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
        log.error("Invalid payment amount for orderId: {}, amount: {}",
            command.getOrderId(), command.getAmount());
        publishPaymentFailed(command.getOrderId(), "Invalid payment amount");
        return;
      }

      if (command.getUserId() == null) {
        log.error("Missing userId for payment processing, orderId: {}", command.getOrderId());
        publishPaymentFailed(command.getOrderId(), "Missing user information");
        return;
      }

      // ✅ Simulate payment processing (you could add actual logic here)
      log.debug("Validating payment details for orderId: {}", command.getOrderId());

      // Simulate some processing time (optional)
      Thread.sleep(100);

      // ✅ Generate transaction ID
      String transactionId = generateTransactionId();

      log.info("Payment processed successfully for orderId: {}, transactionId: {}",
          command.getOrderId(), transactionId);

      // ✅ Publish success event
      eventGateway.publish(PaymentProcessedEvent.builder()
          .orderId(command.getOrderId())
          .userId(command.getUserId())
          .amount(command.getAmount())
          .currency(command.getCurrency())
          .paymentMethod(command.getPaymentMethod())
          .transactionId(transactionId)
          .processedAt(LocalDateTime.now())
          .build());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Payment processing interrupted for orderId: {}", command.getOrderId(), e);
      publishPaymentFailed(command.getOrderId(), "Payment processing interrupted");
    } catch (Exception e) {
      log.error("Unexpected error during payment processing for orderId: {}", command.getOrderId(),
          e);
      publishPaymentFailed(command.getOrderId(), "Payment processing failed: " + e.getMessage());
    }
  }

  private String generateTransactionId() {
    // ✅ Simple transaction ID generation
    return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private void publishPaymentFailed(String orderId, String reason) {
    log.warn("Publishing payment failed event for orderId: {}, reason: {}", orderId, reason);

    eventGateway.publish(PaymentFailedEvent.builder()
        .orderId(orderId)
        .reason(reason)
        .build());
  }
}