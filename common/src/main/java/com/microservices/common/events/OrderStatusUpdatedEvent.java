package com.microservices.common.events;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdatedEvent {

  private String orderId;
  private String previousStatus;
  private String newStatus;
  private String reason;
  private String updatedBy;
  private LocalDateTime updatedAt;
}
