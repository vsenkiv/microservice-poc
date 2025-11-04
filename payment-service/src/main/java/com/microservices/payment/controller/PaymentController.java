package com.microservices.payment.controller;

import com.microservices.payment.dto.PaymentResponse;
import com.microservices.payment.model.OrderEvent;
import com.microservices.payment.model.PaymentTransaction;
import com.microservices.payment.service.PaymentProcessingService;
import com.microservices.payment.service.PaymentQueryService;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentQueryService queryService;
  private final PaymentProcessingService processingService;

  // Streaming endpoint - Returns all order events as Server-Sent Events
  @GetMapping(value = "/orders/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<OrderEvent>> streamOrderEvents() {
    log.info("Starting order events stream");

    return queryService.getAllOrderEvents()
        .delayElements(Duration.ofMillis(100)) // Throttle to avoid overwhelming client
        .map(event -> ServerSentEvent.<OrderEvent>builder()
            .id(event.getId())
            .event("order-event")
            .data(event)
            .build())
        .doOnNext(event -> log.debug("Streaming event: {}", event.id()))
        .doOnComplete(() -> log.info("Completed streaming order events"))
        .doOnError(error -> log.error("Error streaming events: {}", error.getMessage()));
  }

  // Stream order events for specific user
  @GetMapping(value = "/orders/stream/user/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<OrderEvent>> streamUserOrderEvents(@PathVariable Long userId) {
    log.info("Starting order events stream for user: {}", userId);

    return queryService.getOrderEventsByUserId(userId)
        .delayElements(Duration.ofMillis(50))
        .map(event -> ServerSentEvent.<OrderEvent>builder()
            .id(event.getId())
            .event("user-order-event")
            .data(event)
            .build());
  }

  // Get all order events (paginated with reactive)
  @GetMapping("/orders")
  public Flux<OrderEvent> getAllOrderEvents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    log.info("Fetching order events: page={}, size={}", page, size);

    return queryService.getAllOrderEvents()
        .skip((long) page * size)
        .take(size);
  }

  // Get order events by user ID
  @GetMapping("/orders/user/{userId}")
  public Flux<OrderEvent> getOrderEventsByUser(@PathVariable Long userId) {
    log.info("Fetching order events for user: {}", userId);
    return queryService.getOrderEventsByUserId(userId);
  }

  // Get order events by order ID
  @GetMapping("/orders/{orderId}")
  public Flux<OrderEvent> getOrderEventsByOrderId(@PathVariable Long orderId) {
    log.info("Fetching order events for order: {}", orderId);
    return queryService.getOrderEventsByOrderId(orderId);
  }

  // Get order events by date range
  @GetMapping("/orders/date-range")
  public Flux<OrderEvent> getOrderEventsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
    log.info("Fetching order events between {} and {}", start, end);
    return queryService.getOrderEventsByDateRange(start, end);
  }

  // Get payment transactions
  @GetMapping("/transactions")
  public Flux<PaymentResponse> getAllTransactions() {
    log.info("Fetching all payment transactions");
    return queryService.getAllTransactions()
        .map(this::mapToPaymentResponse);
  }

  // Get payment transaction by ID
  @GetMapping("/transactions/{transactionId}")
  public Mono<PaymentResponse> getTransaction(@PathVariable String transactionId) {
    log.info("Fetching transaction: {}", transactionId);
    return queryService.getTransactionById(transactionId)
        .map(this::mapToPaymentResponse);
  }

  // Get transactions by order ID
  @GetMapping("/transactions/order/{orderId}")
  public Flux<PaymentResponse> getTransactionsByOrderId(@PathVariable Long orderId) {
    log.info("Fetching transactions for order: {}", orderId);
    return queryService.getTransactionsByOrderId(orderId)
        .map(this::mapToPaymentResponse);
  }

  // Stream real-time payment updates
  @GetMapping(value = "/transactions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<PaymentResponse>> streamPaymentTransactions() {
    log.info("Starting payment transactions stream");

    return Flux.interval(Duration.ofSeconds(2))
        .flatMap(tick -> queryService.getRecentTransactions(Duration.ofMinutes(5)))
        .map(transaction -> ServerSentEvent.<PaymentResponse>builder()
            .id(transaction.getTransactionId())
            .event("payment-transaction")
            .data(mapToPaymentResponse(transaction))
            .build());
  }

  // Retry failed payment
  @PostMapping("/transactions/{transactionId}/retry")
  public Mono<PaymentResponse> retryPayment(@PathVariable String transactionId) {
    log.info("Retrying payment for transaction: {}", transactionId);
    return processingService.retryPayment(transactionId)
        .map(this::mapToPaymentResponse);
  }

  // Get statistics
  @GetMapping("/stats")
  public Mono<PaymentStats> getPaymentStats() {
    log.info("Fetching payment statistics");
    return queryService.getPaymentStatistics();
  }

  private PaymentResponse mapToPaymentResponse(PaymentTransaction transaction) {
    return PaymentResponse.builder()
        .id(transaction.getId())
        .transactionId(transaction.getTransactionId())
        .orderId(transaction.getOrderId())
        .userId(transaction.getUserId())
        .amount(transaction.getAmount())
        .currency(transaction.getCurrency())
        .paymentMethod(transaction.getPaymentMethod())
        .status(transaction.getStatus())
        .initiatedAt(transaction.getInitiatedAt())
        .completedAt(transaction.getCompletedAt())
        .build();
  }

  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class PaymentStats {

    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Long pendingTransactions;
    private Double successRate;
    private java.math.BigDecimal totalAmount;
  }
}