package com.microservices.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailOrderCommand {

  @TargetAggregateIdentifier
  private String orderId;

  private String reason;

  private String failedBy;
}
