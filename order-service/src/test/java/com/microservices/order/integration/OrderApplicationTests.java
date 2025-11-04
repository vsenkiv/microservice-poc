package com.microservices.order.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
class OrderApplicationTests {

  @Test
  void contextLoads() {
    // Verify that application context can start with mock Axon components
  }

}
