package com.microservices.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

  @Id
  private String id;

  private Long orderId;
  private Long userId;
  private String userEmail;
  private BigDecimal totalAmount;
  private String orderStatus;
  private List<OrderItem> items;
  private String eventType; // CREATED, UPDATED, CANCELLED
  private LocalDateTime eventTimestamp;
  private LocalDateTime receivedAt;
  private String paymentStatus; // PENDING, PROCESSING, COMPLETED, FAILED
  private String paymentMethod;
  private String transactionId;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItem {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
  }
}