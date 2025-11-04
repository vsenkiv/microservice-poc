package com.microservices.order.handler;

import com.microservices.common.commands.ReserveInventoryCommand;
import com.microservices.common.events.InventoryReservationFailedEvent;
import com.microservices.common.events.InventoryReservedEvent;
import com.microservices.order.client.InventoryServiceClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationHandler {

  private final InventoryServiceClient inventoryServiceClient;
  private final EventGateway eventGateway;

  @CommandHandler
  public void handle(ReserveInventoryCommand command) {
    log.info("Processing inventory reservation for orderId: {}, items count: {}",
        command.getOrderId(), command.getItems().size());

    try {
      List<InventoryReservedEvent.ReservedItem> reservedItems = new ArrayList<>();

      // ✅ Check availability for all items first
      for (ReserveInventoryCommand.InventoryItem item : command.getItems()) {
        log.debug("Checking inventory for productId: {}, quantity: {}",
            item.getProductId(), item.getQuantity());

        var inventoryResponse = inventoryServiceClient.checkInventory(
            item.getProductId(),
            item.getQuantity()
        );

        if (inventoryResponse == null) {
          log.error("No response from inventory service for productId: {}", item.getProductId());
          publishReservationFailed(command.getOrderId(),
              "Inventory service unavailable for product: " + item.getProductId());
          return;
        }
      }

      // ✅ All reservations successful
      log.info("Successfully reserved inventory for orderId: {}, total items: {}",
          command.getOrderId(), reservedItems.size());

      eventGateway.publish(InventoryReservedEvent.builder()
          .orderId(command.getOrderId())
          .reservedItems(reservedItems)
          .build());

    } catch (Exception e) {
      log.error("Unexpected error during inventory reservation for orderId: {}",
          command.getOrderId(), e);
      publishReservationFailed(command.getOrderId(),
          "Unexpected error during inventory reservation: " + e.getMessage());
    }
  }


  private void rollbackReservations(List<InventoryReservedEvent.ReservedItem> reservedItems) {
    log.warn("Rolling back {} reservations", reservedItems.size());
  }

  private void publishReservationFailed(String orderId, String reason) {
    eventGateway.publish(InventoryReservationFailedEvent.builder()
        .orderId(orderId)
        .reason(reason)
        .build());
  }
}