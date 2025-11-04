package com.microservices.payment.service;

import com.microservices.payment.controller.PaymentController;
import com.microservices.payment.model.OrderEvent;
import com.microservices.payment.model.PaymentTransaction;
import com.microservices.payment.repository.OrderEventRepository;
import com.microservices.payment.repository.PaymentTransactionRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueryService {

  private final OrderEventRepository orderEventRepository;
  private final PaymentTransactionRepository transactionRepository;

  public Flux<OrderEvent> getAllOrderEvents() {
    return orderEventRepository.findAll();
  }

  public Flux<OrderEvent> getOrderEventsByUserId(Long userId) {
    return orderEventRepository.findByUserIdOrderByEventTimestampDesc(userId);
  }

  public Flux<OrderEvent> getOrderEventsByOrderId(Long orderId) {
    return orderEventRepository.findByOrderId(orderId);
  }

  public Flux<OrderEvent> getOrderEventsByDateRange(LocalDateTime start, LocalDateTime end) {
    return orderEventRepository.findByEventTimestampBetween(start, end);
  }

  public Flux<PaymentTransaction> getAllTransactions() {
    return transactionRepository.findAll();
  }

  public Mono<PaymentTransaction> getTransactionById(String transactionId) {
    return transactionRepository.findByTransactionId(transactionId);
  }

  public Flux<PaymentTransaction> getTransactionsByOrderId(Long orderId) {
    return transactionRepository.findByOrderId(orderId);
  }

  public Flux<PaymentTransaction> getTransactionsByUserId(Long userId) {
    return transactionRepository.findByUserId(userId);
  }

  public Flux<PaymentTransaction> getRecentTransactions(Duration duration) {
    LocalDateTime since = LocalDateTime.now().minus(duration);
    return transactionRepository.findAll()
        .filter(transaction -> transaction.getInitiatedAt().isAfter(since));
  }

  public Mono<PaymentController.PaymentStats> getPaymentStatistics() {
    return Flux.merge(
            transactionRepository.count(),
            transactionRepository.findByStatus(PaymentTransaction.PaymentStatus.COMPLETED).count(),
            transactionRepository.findByStatus(PaymentTransaction.PaymentStatus.FAILED).count(),
            transactionRepository.findByStatus(PaymentTransaction.PaymentStatus.PENDING).count(),
            transactionRepository.findAll()
                .filter(t -> t.getStatus() == PaymentTransaction.PaymentStatus.COMPLETED)
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        ).collectList()
        .map(results -> {
          Long total = (Long) results.get(0);
          Long successful = (Long) results.get(1);
          Long failed = (Long) results.get(2);
          Long pending = (Long) results.get(3);
          BigDecimal totalAmount = (BigDecimal) results.get(4);

          double successRate = total > 0 ? (successful * 100.0 / total) : 0.0;

          return PaymentController.PaymentStats.builder()
              .totalTransactions(total)
              .successfulTransactions(successful)
              .failedTransactions(failed)
              .pendingTransactions(pending)
              .successRate(successRate)
              .totalAmount(totalAmount)
              .build();
        });
  }
}