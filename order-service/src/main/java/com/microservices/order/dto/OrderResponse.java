package com.microservices.order.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")  // ✅ Add this
public class OrderResponse {

  private static final long serialVersionUID = 1L;  // ✅ Add this

  private String id;
  private Long userId;
  private String userName;
  private BigDecimal totalAmount;
  private String status;
  private String paymentStatus;
  private List<OrderItemResponse> items;

  @Data
  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
  public static class OrderItemResponse {

    private static final long serialVersionUID = 1L;  // ✅ Add this

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;


  }
}
