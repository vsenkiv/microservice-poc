package com.microservices.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payment_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

  @Id
  private String id;

  private String transactionId;
  private Long orderId;
  private Long userId;
  private BigDecimal amount;
  private String currency;
  private String paymentMethod;
  private PaymentStatus status;
  private String gatewayResponse;
  private String errorMessage;
  private LocalDateTime initiatedAt;
  private LocalDateTime completedAt;
  private Integer retryCount;

  public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
  }
}