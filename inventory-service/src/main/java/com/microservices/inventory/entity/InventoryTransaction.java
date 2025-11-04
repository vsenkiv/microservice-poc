package com.microservices.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "inventory_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "previous_quantity", nullable = false)
  private Integer previousQuantity;

  @Column(name = "new_quantity", nullable = false)
  private Integer newQuantity;

  @Column(name = "reference_id")
  private Long referenceId; // Order ID or other reference

  @Column(name = "reference_type")
  private String referenceType; // ORDER, RESTOCK, ADJUSTMENT, etc.

  @Column(length = 500)
  private String notes;

  @Column(name = "performed_by")
  private String performedBy;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public enum TransactionType {
    RESERVE,        // Reserve stock for order
    RELEASE,        // Release reserved stock (order cancelled)
    FULFILL,        // Fulfill order (reduce actual stock)
    RESTOCK,        // Add new stock
    ADJUSTMENT,     // Manual adjustment
    DAMAGE,         // Damaged goods
    RETURN          // Customer return
  }
}