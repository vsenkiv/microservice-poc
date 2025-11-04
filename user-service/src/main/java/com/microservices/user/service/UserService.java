package com.microservices.user.service;

import com.microservices.user.dto.AddressDto;
import com.microservices.user.dto.CreateUserRequest;
import com.microservices.user.dto.UpdateUserRequest;
import com.microservices.user.dto.UserProfileDto;
import com.microservices.user.dto.UserResponse;
import com.microservices.user.entity.User;
import com.microservices.user.entity.UserAddress;
import com.microservices.user.entity.UserProfile;
import com.microservices.user.exception.UserNotFoundException;
import com.microservices.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    log.info("Creating user with username: {}", request.getUsername());

    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists: " + request.getUsername());
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists: " + request.getEmail());
    }

    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .phoneNumber(request.getPhoneNumber())
        .status(User.UserStatus.ACTIVE)
        .build();

    // Add profile if provided
    if (request.getProfile() != null) {
      UserProfile profile = mapToUserProfile(request.getProfile(), user);
      user.setProfile(profile);
    }

    // Add address if provided
    if (request.getAddress() != null) {
      UserAddress address = mapToUserAddress(request.getAddress(), user);
      address.setIsDefault(true);
      user.getAddresses().add(address);
    }

    User savedUser = userRepository.save(user);
    log.info("Successfully created user with ID: {}", savedUser.getId());

    return mapToUserResponse(savedUser);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserById(Long id) {
    log.info("Fetching user with ID: {}", id);
    User user = userRepository.findByIdWithDetails(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    return mapToUserResponse(user);
  }

  @Transactional(readOnly = true)
  public UserResponse getUserByUsername(String username) {
    log.info("Fetching user with username: {}", username);
    User user = userRepository.findByUsernameWithDetails(username)
        .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

    return mapToUserResponse(user);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getAllUsers() {
    log.info("Fetching all users");
    return userRepository.findAll().stream()
        .map(this::mapToUserResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public UserResponse updateUser(Long id, UpdateUserRequest request) {
    log.info("Updating user with ID: {}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Email already exists: " + request.getEmail());
      }
      user.setEmail(request.getEmail());
    }

    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }

    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }

    if (request.getPhoneNumber() != null) {
      user.setPhoneNumber(request.getPhoneNumber());
    }

    if (request.getProfile() != null) {
      updateUserProfile(user, request.getProfile());
    }

    User savedUser = userRepository.save(user);
    log.info("Successfully updated user with ID: {}", savedUser.getId());

    return mapToUserResponse(savedUser);
  }

  @Transactional
  public void deleteUser(Long id) {
    log.info("Deleting user with ID: {}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

    user.setStatus(User.UserStatus.INACTIVE);
    userRepository.save(user);

    log.info("Successfully deactivated user with ID: {}", id);
  }

  @Transactional
  public UserResponse addAddress(Long userId, AddressDto addressDto) {
    log.info("Adding address for user with ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    UserAddress address = mapToUserAddress(addressDto, user);

    // If this is the first address or marked as default, make it default
    if (user.getAddresses().isEmpty() || Boolean.TRUE.equals(addressDto.getIsDefault())) {
      // Remove default from other addresses
      user.getAddresses().forEach(addr -> addr.setIsDefault(false));
      address.setIsDefault(true);
    }

    user.getAddresses().add(address);
    User savedUser = userRepository.save(user);

    log.info("Successfully added address for user with ID: {}", userId);
    return mapToUserResponse(savedUser);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> searchUsers(String name) {
    log.info("Searching users with name containing: {}", name);
    return userRepository.findByNameContaining(name).stream()
        .map(this::mapToUserResponse)
        .collect(Collectors.toList());
  }

  private UserResponse mapToUserResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phoneNumber(user.getPhoneNumber())
        .status(user.getStatus())
        .profile(user.getProfile() != null ? mapToUserProfileDto(user.getProfile()) : null)
        .addresses(user.getAddresses().stream()
            .map(this::mapToAddressDto)
            .collect(Collectors.toList()))
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  private UserProfile mapToUserProfile(UserProfileDto dto, User user) {
    return UserProfile.builder()
        .user(user)
        .dateOfBirth(dto.getDateOfBirth())
        .gender(dto.getGender())
        .occupation(dto.getOccupation())
        .bio(dto.getBio())
        .preferredLanguage(dto.getPreferredLanguage())
        .marketingConsent(dto.getMarketingConsent())
        .build();
  }

  private UserProfileDto mapToUserProfileDto(UserProfile profile) {
    return UserProfileDto.builder()
        .dateOfBirth(profile.getDateOfBirth())
        .gender(profile.getGender())
        .occupation(profile.getOccupation())
        .bio(profile.getBio())
        .preferredLanguage(profile.getPreferredLanguage())
        .marketingConsent(profile.getMarketingConsent())
        .build();
  }

  private UserAddress mapToUserAddress(AddressDto dto, User user) {
    return UserAddress.builder()
        .user(user)
        .street(dto.getStreet())
        .city(dto.getCity())
        .state(dto.getState())
        .postalCode(dto.getPostalCode())
        .country(dto.getCountry())
        .type(dto.getType() != null ? dto.getType() : UserAddress.AddressType.HOME)
        .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
        .build();
  }

  private AddressDto mapToAddressDto(UserAddress address) {
    return AddressDto.builder()
        .id(address.getId())
        .street(address.getStreet())
        .city(address.getCity())
        .state(address.getState())
        .postalCode(address.getPostalCode())
        .country(address.getCountry())
        .type(address.getType())
        .isDefault(address.getIsDefault())
        .build();
  }

  private void updateUserProfile(User user, UserProfileDto profileDto) {
    UserProfile profile = user.getProfile();
    if (profile == null) {
      profile = mapToUserProfile(profileDto, user);
      user.setProfile(profile);
    } else {
      if (profileDto.getDateOfBirth() != null) {
        profile.setDateOfBirth(profileDto.getDateOfBirth());
      }
      if (profileDto.getGender() != null) {
        profile.setGender(profileDto.getGender());
      }
      if (profileDto.getOccupation() != null) {
        profile.setOccupation(profileDto.getOccupation());
      }
      if (profileDto.getBio() != null) {
        profile.setBio(profileDto.getBio());
      }
      if (profileDto.getPreferredLanguage() != null) {
        profile.setPreferredLanguage(profileDto.getPreferredLanguage());
      }
      if (profileDto.getMarketingConsent() != null) {
        profile.setMarketingConsent(profileDto.getMarketingConsent());
      }
    }
  }
}