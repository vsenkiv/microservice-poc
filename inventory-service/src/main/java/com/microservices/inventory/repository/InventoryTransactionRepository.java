package com.microservices.inventory.repository;

import com.microservices.inventory.entity.InventoryTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

  List<InventoryTransaction> findByProductId(Long productId);

  List<InventoryTransaction> findByReferenceIdAndReferenceType(Long referenceId,
      String referenceType);

  @Query("SELECT t FROM InventoryTransaction t WHERE t.product.id = :productId AND t.createdAt BETWEEN :startDate AND :endDate")
  List<InventoryTransaction> findByProductIdAndDateRange(
      @Param("productId") Long productId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  List<InventoryTransaction> findByType(InventoryTransaction.TransactionType type);
}