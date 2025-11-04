package com.microservices.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

  private String name;
  private String description;
  private String category;

  @DecimalMin(value = "0.0", message = "Price must be positive")
  private BigDecimal price;

  @Min(value = 0, message = "Reorder level must be non-negative")
  private Integer reorderLevel;

  @Min(value = 0, message = "Reorder quantity must be non-negative")
  private Integer reorderQuantity;

  private String imageUrl;
  private Double weight;
  private String weightUnit;
}