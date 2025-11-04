package com.microservices.common.events;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {

  private String orderId;
  private Long userId;
  private BigDecimal amount;
  private String reason;
  private String errorCode;
}