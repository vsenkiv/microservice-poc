// order-service/src/main/java/com/microservices/inventory/dto/ReservationRequest.java
package com.microservices.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

  private Long orderId;
  private Long productId;
  private Integer quantity;
}