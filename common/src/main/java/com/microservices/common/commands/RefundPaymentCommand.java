package com.microservices.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private String transactionId;
  private String reason;
}