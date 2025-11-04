package com.microservices.common.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private String reason;
  private String cancelledBy;
}
