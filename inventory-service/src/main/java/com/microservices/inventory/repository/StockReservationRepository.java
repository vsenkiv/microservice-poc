package com.microservices.inventory.repository;

import com.microservices.inventory.entity.StockReservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

  List<StockReservation> findByOrderId(Long orderId);

  List<StockReservation> findByProductId(Long productId);

  List<StockReservation> findByStatus(StockReservation.ReservationStatus status);

  Optional<StockReservation> findByOrderIdAndProductId(Long orderId, Long productId);

  @Query("SELECT r FROM StockReservation r WHERE r.status = 'ACTIVE' AND r.expiresAt < :now")
  List<StockReservation> findExpiredReservations(@Param("now") LocalDateTime now);

  @Query("SELECT SUM(r.quantity) FROM StockReservation r WHERE r.product.id = :productId AND r.status = 'ACTIVE'")
  Integer getTotalReservedQuantity(@Param("productId") Long productId);
}