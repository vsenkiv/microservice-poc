package com.microservices.common.events;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefundedEvent {

  private String orderId;
  private String transactionId;
  private String refundId;
  private LocalDateTime refundedAt;
}
