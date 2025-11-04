package com.microservices.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {
    // Only exclude Axon's JPA auto-configurations, keep regular JPA
    org.axonframework.springboot.autoconfig.JpaAutoConfiguration.class,
    org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration.class
})
@EntityScan(basePackages = {
    "com.microservices.order.entity",  // Your business entities
    "com.microservices.common.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.microservices.order.repository",  // Your business repositories
    "com.microservices.common.repository"
})
@ComponentScan(basePackages = "com.microservices")
public class OrderApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderApplication.class, args);
  }

}
