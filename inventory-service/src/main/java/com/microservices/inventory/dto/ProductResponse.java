package com.microservices.inventory.dto;

import com.microservices.inventory.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  private Long id;
  private String sku;
  private String name;
  private String description;
  private String category;
  private BigDecimal price;
  private Integer quantity;
  private Integer reservedQuantity;
  private Integer availableQuantity;
  private Integer reorderLevel;
  private Integer reorderQuantity;
  private Product.ProductStatus status;
  private String imageUrl;
  private Double weight;
  private String weightUnit;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}