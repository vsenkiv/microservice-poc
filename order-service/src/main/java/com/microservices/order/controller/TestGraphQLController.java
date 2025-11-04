package com.microservices.order.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class TestGraphQLController {

  @QueryMapping
  public String ping() {
    log.info("🔥 GraphQL ping endpoint called!");
    return "pong";
  }
}
