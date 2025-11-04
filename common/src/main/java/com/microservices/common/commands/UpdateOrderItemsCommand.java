package com.microservices.common.commands;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemsCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private List<OrderItemUpdate> itemUpdates;
  private String updatedBy;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemUpdate {

    private Long productId;
    private Integer newQuantity;
    private BigDecimal newPrice;
    private String action; // ADD, REMOVE, UPDATE
  }
}
