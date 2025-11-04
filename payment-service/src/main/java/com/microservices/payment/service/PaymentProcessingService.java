package com.microservices.payment.service;

import com.microservices.payment.model.OrderEvent;
import com.microservices.payment.model.PaymentTransaction;
import com.microservices.payment.repository.PaymentTransactionRepository;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService {

  private final PaymentTransactionRepository transactionRepository;
  private final Random random = new Random();

  public Mono<PaymentTransaction> processPayment(OrderEvent orderEvent) {
    log.info("Processing payment for order: {}, amount: {}",
        orderEvent.getOrderId(), orderEvent.getTotalAmount());

    return Mono.defer(() -> {
          // Simulate payment processing
          PaymentTransaction transaction = PaymentTransaction.builder()
              .transactionId(UUID.randomUUID().toString())
              .orderId(orderEvent.getOrderId())
              .userId(orderEvent.getUserId())
              .amount(orderEvent.getTotalAmount())
              .currency("USD")
              .paymentMethod(determinePaymentMethod())
              .status(PaymentTransaction.PaymentStatus.PROCESSING)
              .initiatedAt(LocalDateTime.now())
              .retryCount(0)
              .build();

          return transactionRepository.save(transaction);
        })
        .delayElement(
            java.time.Duration.ofMillis(random.nextInt(1000) + 500)) // Simulate processing time
        .flatMap(transaction -> {
          // Simulate payment gateway response (90% success rate)
          boolean success = random.nextDouble() < 0.9;

          if (success) {
            transaction.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
            transaction.setGatewayResponse("APPROVED");
            transaction.setCompletedAt(LocalDateTime.now());
            log.info("Payment successful for order: {}", orderEvent.getOrderId());
          } else {
            transaction.setStatus(PaymentTransaction.PaymentStatus.FAILED);
            transaction.setGatewayResponse("DECLINED");
            transaction.setErrorMessage("Insufficient funds or card declined");
            log.warn("Payment failed for order: {}", orderEvent.getOrderId());
          }

          return transactionRepository.save(transaction);
        })
        .doOnError(error -> log.error("Payment processing error for order: {}, error: {}",
            orderEvent.getOrderId(), error.getMessage()));
  }

  private String determinePaymentMethod() {
    String[] methods = {"CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"};
    return methods[random.nextInt(methods.length)];
  }

  public Mono<PaymentTransaction> retryPayment(String transactionId) {
    return transactionRepository.findByTransactionId(transactionId)
        .flatMap(transaction -> {
          if (transaction.getStatus() != PaymentTransaction.PaymentStatus.FAILED) {
            return Mono.error(new IllegalStateException(
                "Can only retry failed transactions"));
          }

          transaction.setStatus(PaymentTransaction.PaymentStatus.PROCESSING);
          transaction.setRetryCount(transaction.getRetryCount() + 1);

          return transactionRepository.save(transaction)
              .delayElement(java.time.Duration.ofMillis(500))
              .flatMap(saved -> {
                // Retry logic (higher success rate on retry)
                boolean success = random.nextDouble() < 0.95;

                if (success) {
                  saved.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
                  saved.setGatewayResponse("APPROVED_ON_RETRY");
                  saved.setCompletedAt(LocalDateTime.now());
                  saved.setErrorMessage(null);
                } else {
                  saved.setStatus(PaymentTransaction.PaymentStatus.FAILED);
                  saved.setErrorMessage("Retry failed");
                }

                return transactionRepository.save(saved);
              });
        });
  }
}