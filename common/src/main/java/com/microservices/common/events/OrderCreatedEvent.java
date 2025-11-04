package com.microservices.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

  private String orderId;
  private Long userId;
  private List<OrderItem> items;
  private BigDecimal totalAmount;
  private String currency;
  private String paymentMethod;
  private LocalDateTime createdAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrderItem {

    private Long productId;
    private Integer quantity;
    private BigDecimal price;
  }
}
