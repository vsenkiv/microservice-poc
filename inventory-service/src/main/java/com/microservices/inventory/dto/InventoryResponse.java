package com.microservices.inventory.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

  private Long productId;
  private String productName;
  private Integer availableQuantity;
  private boolean available;
  private BigDecimal price;
}