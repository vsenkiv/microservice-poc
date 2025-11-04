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
public class PaymentProcessedEvent {

  private String orderId;
  private Long userId;
  private BigDecimal amount;
  private String currency;
  private String transactionId;
  private String paymentMethod;
  private LocalDateTime processedAt;
}