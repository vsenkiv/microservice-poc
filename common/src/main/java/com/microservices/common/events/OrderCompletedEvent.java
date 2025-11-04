package com.microservices.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCompletedEvent {

  private String orderId;
  private Long userId;
  private BigDecimal totalAmount;
  private String transactionId;
  private LocalDateTime completedAt;
}