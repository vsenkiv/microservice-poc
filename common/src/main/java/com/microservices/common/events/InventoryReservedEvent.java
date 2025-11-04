package com.microservices.common.events;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservedEvent {

  private String orderId;
  private List<ReservedItem> reservedItems;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ReservedItem {

    private Long productId;
    private Integer quantity;
    private String reservationId;
  }
}