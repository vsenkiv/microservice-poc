package com.microservices.user.dto;

import com.microservices.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
  private LocalDate dateOfBirth;
  private UserProfile.Gender gender;
  private String occupation;
  private String bio;
  private String profileImageUrl;
  private String preferredLanguage;
  private Boolean marketingConsent;
}