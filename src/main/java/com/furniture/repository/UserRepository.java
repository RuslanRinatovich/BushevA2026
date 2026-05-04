package com.furniture.repository;

import com.furniture.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndEnabledTrue(String username);
    boolean existsByUsername(String username);
}