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
public class OrderCancelledEvent {

  private String orderId;
  private String reason;
  private String cancelledBy;
  private LocalDateTime cancelledAt;
}
