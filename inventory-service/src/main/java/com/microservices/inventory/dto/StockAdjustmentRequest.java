package com.microservices.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

  @NotNull(message = "Quantity is required")
  private Integer quantity;

  @NotNull(message = "Adjustment type is required")
  private String adjustmentType; // RESTOCK, ADJUSTMENT, DAMAGE, RETURN

  private String notes;
}