package com.microservices.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateOrderRequest {

  @NotNull(message = "User ID cannot be null")
  @Positive(message = "User ID must be positive")
  private Long userId;

  @NotEmpty(message = "Order items cannot be empty")
  @Valid
  private List<OrderItemRequest> items;


  @Data
  public static class OrderItemRequest {

    @NotNull(message = "Product ID cannot be null")
    @Positive(message = "Product ID must be positive")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

  }
}