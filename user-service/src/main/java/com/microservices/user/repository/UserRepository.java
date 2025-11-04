package com.microservices.user.repository;

import com.microservices.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  List<User> findByStatus(User.UserStatus status);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile LEFT JOIN FETCH u.addresses WHERE u.id = :id")
  Optional<User> findByIdWithDetails(@Param("id") Long id);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile LEFT JOIN FETCH u.addresses WHERE u.username = :username")
  Optional<User> findByUsernameWithDetails(@Param("username") String username);

  @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
  List<User> findByNameContaining(@Param("name") String name);
}