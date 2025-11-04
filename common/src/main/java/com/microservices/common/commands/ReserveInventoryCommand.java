package com.microservices.common.commands;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private Long userId;
  private List<InventoryItem> items;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class InventoryItem {

    private Long productId;
    private Integer quantity;
  }
}
