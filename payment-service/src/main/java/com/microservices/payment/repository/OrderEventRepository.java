package com.microservices.payment.repository;

import com.microservices.payment.model.OrderEvent;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderEventRepository extends ReactiveMongoRepository<OrderEvent, String> {

  Flux<OrderEvent> findByUserId(Long userId);

  Flux<OrderEvent> findByOrderId(Long orderId);

  Flux<OrderEvent> findByEventType(String eventType);

  Flux<OrderEvent> findByPaymentStatus(String paymentStatus);

  @Query("{ 'eventTimestamp': { $gte: ?0, $lte: ?1 } }")
  Flux<OrderEvent> findByEventTimestampBetween(LocalDateTime start, LocalDateTime end);

  Flux<OrderEvent> findByUserIdOrderByEventTimestampDesc(Long userId);

  @Query("{ 'totalAmount': { $gte: ?0 } }")
  Flux<OrderEvent> findByTotalAmountGreaterThan(Double amount);
}