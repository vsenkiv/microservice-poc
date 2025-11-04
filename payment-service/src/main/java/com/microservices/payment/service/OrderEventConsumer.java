package com.microservices.payment.service;

import com.microservices.payment.dto.OrderEventDto;
import com.microservices.payment.model.OrderEvent;
import com.microservices.payment.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

  private final OrderEventRepository orderEventRepository;
  private final PaymentProcessingService paymentProcessingService;

  @KafkaListener(
      topics = "${spring.kafka.topic.order-events}",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void consumeOrderEvents(
      @Payload List<OrderEventDto> events,
      @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
      @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

    log.info("Received batch of {} order events from partitions: {}", events.size(), partitions);

    // Convert DTOs to entities
    List<OrderEvent> orderEvents = events.stream()
        .map(this::mapToOrderEvent)
        .collect(Collectors.toList());

    // Save batch to MongoDB (reactive)
    Flux.fromIterable(orderEvents)
        .flatMap(orderEventRepository::save)
        .doOnNext(saved -> log.debug("Saved order event: orderId={}, eventId={}",
            saved.getOrderId(), saved.getId()))
        .flatMap(saved -> {
          // Process payment for new orders
          if ("CREATED".equals(saved.getEventType())) {
            return paymentProcessingService.processPayment(saved)
                .doOnSuccess(transaction ->
                    log.info("Payment processed for order: {}, transactionId: {}",
                        saved.getOrderId(), transaction.getTransactionId()))
                .onErrorResume(error -> {
                  log.error("Payment processing failed for order: {}, error: {}",
                      saved.getOrderId(), error.getMessage());
                  return Mono.empty();
                });
          }
          return Mono.just(saved);
        })
        .collectList()
        .doOnSuccess(savedEvents ->
            log.info("Successfully processed batch of {} events", savedEvents.size()))
        .doOnError(error ->
            log.error("Error processing batch: {}", error.getMessage(), error))
        .subscribe();
  }

  private OrderEvent mapToOrderEvent(OrderEventDto dto) {
    return OrderEvent.builder()
        .orderId(dto.getOrderId())
        .userId(dto.getUserId())
        .userEmail(dto.getUserEmail())
        .totalAmount(dto.getTotalAmount())
        .orderStatus(dto.getOrderStatus())
        .items(dto.getItems().stream()
            .map(item -> OrderEvent.OrderItem.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build())
            .collect(Collectors.toList()))
        .eventType(dto.getEventType())
        .eventTimestamp(dto.getEventTimestamp())
        .receivedAt(LocalDateTime.now())
        .paymentStatus("PENDING")
        .transactionId(UUID.randomUUID().toString())
        .build();
  }
}