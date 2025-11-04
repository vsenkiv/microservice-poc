package com.microservices.common.events;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

  private String orderId;
  private String transactionId;
  private String confirmedBy;
  private LocalDateTime confirmedAt;
}
