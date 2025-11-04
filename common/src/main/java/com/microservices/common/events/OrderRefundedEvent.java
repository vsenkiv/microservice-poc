package com.microservices.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundedEvent {

  private String orderId;
  private BigDecimal refundAmount;
  private String reason;
  private String processedBy;
  private LocalDateTime refundedAt;
}
