package com.microservices.common.events;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFailedEvent {

  private String orderId;
  private String reason;
  private String failedStep;
  private LocalDateTime failedAt;
}