package com.microservices.payment.dto;

import com.microservices.payment.model.PaymentTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  private String id;
  private String transactionId;
  private Long orderId;
  private Long userId;
  private BigDecimal amount;
  private String currency;
  private String paymentMethod;
  private PaymentTransaction.PaymentStatus status;
  private LocalDateTime initiatedAt;
  private LocalDateTime completedAt;
}