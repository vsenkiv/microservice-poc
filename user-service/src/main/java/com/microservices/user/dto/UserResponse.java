package com.microservices.user.dto;

import com.microservices.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private User.UserStatus status;
  private UserProfileDto profile;
  private List<AddressDto> addresses;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}