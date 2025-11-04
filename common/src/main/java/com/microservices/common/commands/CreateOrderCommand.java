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
public class CreateOrderCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private Long userId;
  private List<OrderItemRequest> items;
  private String currency = "USD";
  private String paymentMethod = "CREDIT_CARD";

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemRequest {

    private Long productId;
    private Integer quantity;
    private BigDecimal price;
  }
}