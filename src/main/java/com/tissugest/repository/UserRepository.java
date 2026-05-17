package com.tissugest.repository;

import com.tissugest.entity.User;
import com.tissugest.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    java.util.List<User> findByRole(UserRole role);
}
