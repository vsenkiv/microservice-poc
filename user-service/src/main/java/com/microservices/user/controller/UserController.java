package com.microservices.user.controller;

import com.microservices.user.dto.AddressDto;
import com.microservices.user.dto.CreateUserRequest;
import com.microservices.user.dto.UpdateUserRequest;
import com.microservices.user.dto.UserResponse;
import com.microservices.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    log.info("Received user creation request for username: {}", request.getUsername());

    try {
      UserResponse response = userService.createUser(request);
      log.info("Successfully created user with ID: {}", response.getId());
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
      log.error("Failed to create user for username: {} - Error: {}",
          request.getUsername(), e.getMessage(), e);
      throw e;
    }
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
    log.info("Fetching user with ID: {}", userId);
    UserResponse user = userService.getUserById(userId);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/username/{username}")
  public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
    log.info("Fetching user with username: {}", username);
    UserResponse user = userService.getUserByUsername(username);
    return ResponseEntity.ok(user);
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    log.info("Fetching all users");
    List<UserResponse> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @PutMapping("/{userId}")
  public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId,
      @Valid @RequestBody UpdateUserRequest request) {
    log.info("Received user update request for ID: {}", userId);

    try {
      UserResponse response = userService.updateUser(userId, request);
      log.info("Successfully updated user with ID: {}", userId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to update user with ID: {} - Error: {}",
          userId, e.getMessage(), e);
      throw e;
    }
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    log.info("Received user deletion request for ID: {}", userId);

    try {
      userService.deleteUser(userId);
      log.info("Successfully deleted user with ID: {}", userId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Failed to delete user with ID: {} - Error: {}",
          userId, e.getMessage(), e);
      throw e;
    }
  }

  @PostMapping("/{userId}/addresses")
  public ResponseEntity<UserResponse> addAddress(@PathVariable Long userId,
      @Valid @RequestBody AddressDto addressDto) {
    log.info("Adding address for user with ID: {}", userId);

    try {
      UserResponse response = userService.addAddress(userId, addressDto);
      log.info("Successfully added address for user with ID: {}", userId);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to add address for user with ID: {} - Error: {}",
          userId, e.getMessage(), e);
      throw e;
    }
  }

  @GetMapping("/search")
  public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String name) {
    log.info("Searching users with name: {}", name);
    List<UserResponse> users = userService.searchUsers(name);
    return ResponseEntity.ok(users);
  }

  // Health check endpoint for microservice communication
  @GetMapping("/{userId}/exists")
  public ResponseEntity<Boolean> userExists(@PathVariable Long userId) {
    log.info("Checking if user exists with ID: {}", userId);

    try {
      userService.getUserById(userId);
      return ResponseEntity.ok(true);
    } catch (Exception e) {
      return ResponseEntity.ok(false);
    }
  }
}