package com.microservices.order.handler;

import com.microservices.common.commands.ValidateUserCommand;
import com.microservices.common.events.UserValidatedEvent;
import com.microservices.common.events.UserValidationFailedEvent;
import com.microservices.order.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserIntegrationHandler {

  private final UserServiceClient userService;
  private final EventGateway eventGateway;

  @CommandHandler
  public void handle(ValidateUserCommand command) {
    log.info("Validating user for orderId: {}, userId: {}",
        command.getOrderId(), command.getUserId());

    try {
      // ✅ Call external user service or database
      var user = userService.getUserById(command.getUserId());

      if (user == null) {
        log.warn("User not found for userId: {}", command.getUserId());
        eventGateway.publish(new UserValidationFailedEvent(
            command.getOrderId(),
            command.getUserId(),
            "User not found"
        ));
        return;
      }

      // ✅ All validations passed
      log.info("User validation successful for userId: {}", command.getUserId());
      eventGateway.publish(new UserValidatedEvent(
          command.getOrderId(),
          command.getUserId(),
          true,
          user.getUsername(),
          user.getEmail()
      ));

    } catch (Exception e) {
      log.error("Error validating user for userId: {}", command.getUserId(), e);
      eventGateway.publish(new UserValidationFailedEvent(
          command.getOrderId(),
          command.getUserId(),
          "User validation service error: " + e.getMessage()
      ));
    }
  }
}
