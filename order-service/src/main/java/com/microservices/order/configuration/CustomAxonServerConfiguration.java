package com.microservices.order.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "axon.axonserver.enabled", havingValue = "true", matchIfMissing = true)
public class CustomAxonServerConfiguration {

  @Bean
  @Primary
  public Serializer serializer() {
    return JacksonSerializer.builder()
        .objectMapper(objectMapper())
        .build();
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper;
  }

  // Override TokenStore to prevent JPA usage
  @Bean
  @Primary
  @ConditionalOnProperty(value = "axon.axonserver.enabled", havingValue = "true")
  public TokenStore customTokenStore() {
    // Axon Server handles token storage internally
    // Return InMemoryTokenStore as fallback (tokens will be managed by Axon Server)
    return new InMemoryTokenStore();
  }

  // Override SagaStore to prevent JPA usage
  @Bean
  @Primary
  @ConditionalOnProperty(value = "axon.axonserver.enabled", havingValue = "true")
  public SagaStore customSagaStore() {
    // Return InMemorySagaStore as fallback (sagas will be managed by Axon Server)
    return new InMemorySagaStore();
  }

}
