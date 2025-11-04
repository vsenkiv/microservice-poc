package com.microservices.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

  @NotBlank(message = "SKU is required")
  private String sku;

  @NotBlank(message = "Product name is required")
  private String name;

  private String description;

  @NotBlank(message = "Category is required")
  private String category;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.0", message = "Price must be positive")
  private BigDecimal price;

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be non-negative")
  private Integer quantity;

  @Min(value = 0, message = "Reorder level must be non-negative")
  private Integer reorderLevel;

  @Min(value = 0, message = "Reorder quantity must be non-negative")
  private Integer reorderQuantity;

  private String imageUrl;

  private Double weight;
  private String weightUnit;
}