package com.microservices.common.commands;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private Long userId;
  private BigDecimal amount;
  private String currency;
  private String paymentMethod;
}
