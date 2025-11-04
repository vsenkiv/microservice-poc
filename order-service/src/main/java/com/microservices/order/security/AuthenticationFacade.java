package com.microservices.order.security;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public String getCurrentUserEmail() {
    Authentication authentication = getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();

    // Handle OIDC User (OAuth2 Login)
    if (principal instanceof OidcUser) {
      return ((OidcUser) principal).getEmail();
    }

    // Handle JWT (Resource Server)
    if (principal instanceof Jwt) {
      Jwt jwt = (Jwt) principal;
      return jwt.getClaim("email");
    }

    return null;
  }

  public String getCurrentUserId() {
    Authentication authentication = getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof OidcUser) {
      return ((OidcUser) principal).getSubject();
    }

    if (principal instanceof Jwt) {
      Jwt jwt = (Jwt) principal;
      return jwt.getSubject();
    }

    return null;
  }

  public Map<String, Object> getCurrentUserAttributes() {
    Authentication authentication = getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Map.of();
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof OidcUser) {
      return ((OidcUser) principal).getAttributes();
    }

    if (principal instanceof Jwt) {
      return ((Jwt) principal).getClaims();
    }

    return Map.of();
  }
}