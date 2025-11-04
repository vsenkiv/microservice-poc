package com.microservices.payment.repository;

import com.microservices.payment.model.PaymentTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentTransactionRepository extends
    ReactiveMongoRepository<PaymentTransaction, String> {

  Mono<PaymentTransaction> findByTransactionId(String transactionId);

  Flux<PaymentTransaction> findByOrderId(Long orderId);

  Flux<PaymentTransaction> findByUserId(Long userId);

  Flux<PaymentTransaction> findByStatus(PaymentTransaction.PaymentStatus status);

  Mono<PaymentTransaction> findByOrderIdAndStatus(Long orderId,
      PaymentTransaction.PaymentStatus status);
}