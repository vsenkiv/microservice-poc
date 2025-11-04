package com.microservices.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemsUpdatedEvent {

  private String orderId;
  private List<OrderItemChange> itemChanges;
  private BigDecimal newTotalAmount;
  private String updatedBy;
  private LocalDateTime updatedAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemChange {

    private Long productId;
    private Integer oldQuantity;
    private Integer newQuantity;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String action;
  }
}
