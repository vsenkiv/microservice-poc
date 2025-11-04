package com.microservices.common.commands;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundOrderCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private BigDecimal refundAmount;
  private String reason;
  private String processedBy;
}
