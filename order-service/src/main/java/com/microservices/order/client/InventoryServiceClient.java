package com.microservices.order.client;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryServiceClient {

  private final RestClient restClient;
  private final String inventoryServiceUrl;
  private final Tracer tracer;

  public InventoryServiceClient(RestClient.Builder restClientBuilder,
      @Value("${inventory.service.url}") String inventoryServiceUrl,
      Tracer tracer) {
    this.restClient = restClientBuilder.baseUrl(inventoryServiceUrl).build();
    this.inventoryServiceUrl = inventoryServiceUrl;
    this.tracer = tracer;
  }

  public InventoryResponse checkInventory(Long productId, Integer quantity) {
    Span span = tracer.spanBuilder("inventory-check")
        .setAttribute("product.id", productId)
        .setAttribute("quantity.requested", quantity)
        .startSpan();

    try {

      return restClient
          .get()
          .uri(uriBuilder -> uriBuilder
              .path("/api/inventory/check/{productId}")
              .queryParam("quantity", quantity)
              .build(productId))
          .retrieve()
          .body(InventoryResponse.class);
    } finally {
      span.end();
    }
  }

  @Data
  public static class InventoryResponse {

    private Long productId;
    private String productName;
    private Integer availableQuantity;
    private boolean available;
    private Double price;
  }
}