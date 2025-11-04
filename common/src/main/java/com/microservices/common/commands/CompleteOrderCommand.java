package com.microservices.common.commands;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOrderCommand {

  @TargetAggregateIdentifier
  private String orderId;
  private String completedBy;
  private LocalDateTime deliveryDate;
}
