package com.microservices.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand {

  @TargetAggregateIdentifier
  private Long userId;
  private String username;
  private String email;
}
