package com.microservices.order.service;

import com.microservices.order.dto.OrderEventDto;
import com.microservices.order.entity.Order;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

  private final KafkaTemplate<String, OrderEventDto> kafkaTemplate;

  @Value("${spring.kafka.topic.order-events}")
  private String orderEventsTopic;

  public void publishOrderCreatedEvent(Order order, String userEmail) {
    OrderEventDto event = OrderEventDto.builder()
        .orderId(order.getId())
        .userId(order.getUserId())
        .userEmail(userEmail)
        .totalAmount(order.getTotalAmount())
        .orderStatus(order.getStatus().name())
        .items(order.getOrderItems().stream()
            .map(item -> OrderEventDto.OrderItemDto.builder()
                .productId(item.getProductId())
                .productName("Product-" + item.getProductId()) // Get from inventory
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build())
            .collect(Collectors.toList()))
        .eventType("CREATED")
        .eventTimestamp(LocalDateTime.now())
        .build();

    publishEvent(event);
  }

  public void publishOrderUpdatedEvent(Order order, String userEmail) {
    OrderEventDto event = OrderEventDto.builder()
        .orderId(order.getId())
        .userId(order.getUserId())
        .userEmail(userEmail)
        .totalAmount(order.getTotalAmount())
        .orderStatus(order.getStatus().name())
        .eventType("UPDATED")
        .eventTimestamp(LocalDateTime.now())
        .build();

    publishEvent(event);
  }

  public void publishOrderCancelledEvent(Order order, String userEmail) {
    OrderEventDto event = OrderEventDto.builder()
        .orderId(order.getId())
        .userId(order.getUserId())
        .userEmail(userEmail)
        .totalAmount(order.getTotalAmount())
        .orderStatus("CANCELLED")
        .eventType("CANCELLED")
        .eventTimestamp(LocalDateTime.now())
        .build();

    publishEvent(event);
  }

  private void publishEvent(OrderEventDto event) {
    CompletableFuture<SendResult<String, OrderEventDto>> future =
        kafkaTemplate.send(orderEventsTopic, event.getOrderId().toString(), event);

    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info("Published order event: orderId={}, eventType={}, partition={}, offset={}",
            event.getOrderId(), event.getEventType(),
            result.getRecordMetadata().partition(),
            result.getRecordMetadata().offset());
      } else {
        log.error("Failed to publish order event: orderId={}, error={}",
            event.getOrderId(), ex.getMessage(), ex);
      }
    });
  }
}