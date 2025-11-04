package com.microservices.user.dto;

import com.microservices.user.entity.UserAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
  private Long id;

  @NotBlank(message = "Street is required")
  private String street;

  @NotBlank(message = "City is required")
  private String city;

  @NotBlank(message = "State is required")
  private String state;

  @NotBlank(message = "Postal code is required")
  private String postalCode;

  @NotBlank(message = "Country is required")
  private String country;

  private UserAddress.AddressType type;
  private Boolean isDefault;
}