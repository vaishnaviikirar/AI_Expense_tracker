package com.expensetracker.service;

import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService - Business logic for user registration and login.
 *
 * Service Layer Responsibilities:
 * - Business rules (e.g., no duplicate emails)
 * - Coordination between Repository and Security layers
 * - Return clean DTOs (never raw entities)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;          // BCryptPasswordEncoder
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user.
     *
     * Steps:
     * 1. Check if email already exists
     * 2. Encrypt the password with BCrypt
     * 3. Save user to database
     * 4. Generate JWT token
     * 5. Return token + user info
     */
    public AuthResponse register(RegisterRequest request) {
        // Step 1: Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered. Please use a different email or login.");
        }

        // Step 2: Create user entity with encrypted password
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // BCrypt transforms "password123" → "$2a$10$xyz..." (one-way hash)
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Step 3: Save to database
        User savedUser = userRepository.save(user);

        // Step 4: Generate JWT token for immediate login after registration
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Step 5: Return response
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getName(), savedUser.getId());
    }

    /**
     * Login an existing user.
     *
     * Steps:
     * 1. Use AuthenticationManager to verify email + password
     *    (Spring Security handles BCrypt comparison automatically)
     * 2. If credentials wrong → BadCredentialsException thrown (handled by GlobalExceptionHandler)
     * 3. Generate JWT token
     * 4. Return token + user info
     */
    public AuthResponse login(LoginRequest request) {
        // Step 1: Authenticate — this calls UserDetailsService and BCrypt internally
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we reach here, authentication was successful
        // Step 2: Load user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Step 3: Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        // Step 4: Return response
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }
}
