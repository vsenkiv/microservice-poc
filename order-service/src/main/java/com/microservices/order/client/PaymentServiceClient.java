package com.microservices.order.client;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import java.math.BigDecimal;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentServiceClient {

  private final RestClient restClient;
  private final String paymentServiceUrl;
  private final Tracer tracer;

  public PaymentServiceClient(RestClient.Builder restClientBuilder,
      @Value("${payment.service.url}") String paymentServiceUrl,
      Tracer tracer) {
    this.restClient = restClientBuilder.baseUrl(paymentServiceUrl).build();
    this.paymentServiceUrl = paymentServiceUrl;
    this.tracer = tracer;
  }

  public PaymentResponse processPayment(PaymentRequest paymentRequest) {
    Span span = tracer.spanBuilder("payment-processing")
        .setAttribute("order.id", paymentRequest.getOrderId())
        .setAttribute("amount", paymentRequest.getAmount().doubleValue())
        .startSpan();

    try {
      String url = paymentServiceUrl + "/api/payments/process";

      return restClient
          .post()
          .uri(url)
          .body(paymentRequest)
          .retrieve()
          .body(PaymentResponse.class);
    } finally {
      span.end();
    }
  }

  @Data
  public static class PaymentRequest {

    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;

    public PaymentRequest(Long orderId, Long userId, BigDecimal amount) {
      this.orderId = orderId;
      this.userId = userId;
      this.amount = amount;
      this.paymentMethod = "CREDIT_CARD"; // Default
    }

  }

  @Data
  public static class PaymentResponse {

    private Long paymentId;
    private String status;
    private String transactionId;

  }
}
