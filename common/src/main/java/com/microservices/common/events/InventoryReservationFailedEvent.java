package com.microservices.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservationFailedEvent {

  private String orderId;
  private String reason;
  private Long failedProductId;
  private Integer requestedQuantity;
  private Integer availableQuantity;
}
