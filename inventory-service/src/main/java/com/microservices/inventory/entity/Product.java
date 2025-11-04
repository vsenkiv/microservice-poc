package com.microservices.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String sku;

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "reserved_quantity", nullable = false)
  @Builder.Default
  private Integer reservedQuantity = 0;

  @Column(name = "reorder_level", nullable = false)
  @Builder.Default
  private Integer reorderLevel = 10;

  @Column(name = "reorder_quantity", nullable = false)
  @Builder.Default
  private Integer reorderQuantity = 50;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ProductStatus status = ProductStatus.ACTIVE;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(nullable = false)
  @Builder.Default
  private Double weight = 0.0;

  @Column(nullable = false, length = 10)
  @Builder.Default
  private String weightUnit = "kg";

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  // Helper method to get available quantity
  public Integer getAvailableQuantity() {
    return quantity - reservedQuantity;
  }

  // Helper method to check if product is available
  public boolean isAvailable(Integer requestedQuantity) {
    return status == ProductStatus.ACTIVE &&
        getAvailableQuantity() >= requestedQuantity;
  }

  // Helper method to check if reorder is needed
  public boolean needsReorder() {
    return getAvailableQuantity() <= reorderLevel;
  }

  public enum ProductStatus {
    ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
  }
}