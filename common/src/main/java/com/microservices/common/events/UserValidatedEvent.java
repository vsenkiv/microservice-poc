package com.microservices.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserValidatedEvent {

  private String orderId;
  private Long userId;
  private boolean isValid;
  private String userName;
  private String email;
}
