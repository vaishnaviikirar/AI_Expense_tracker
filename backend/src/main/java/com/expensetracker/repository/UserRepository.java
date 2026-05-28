package com.expensetracker.repository;

import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Data access layer for User entity.
 *
 * By extending JpaRepository<User, Long>, we automatically get:
 * - save(), findById(), findAll(), deleteById(), count(), exists(), etc.
 *
 * Spring Data JPA auto-generates SQL from method names!
 * findByEmail() → SELECT * FROM users WHERE email = ?
 * existsByEmail() → SELECT COUNT(*) FROM users WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Returns Optional to force callers to handle "not found" case gracefully.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with this email already exists.
     * Used during registration to prevent duplicate accounts.
     */
    boolean existsByEmail(String email);
}
