package com.expensetracker.controller;

import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - Handles user registration and login.
 *
 * @RestController = @Controller + @ResponseBody (auto-converts return values to JSON)
 * @RequestMapping = Base URL prefix for all endpoints in this class
 * @CrossOrigin = Allow requests from any origin (for frontend)
 *
 * API Endpoints:
 * POST /api/auth/register → Register new user
 * POST /api/auth/login    → Login existing user
 *
 * These are PUBLIC endpoints (no JWT required) — configured in SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * @Valid triggers validation of RegisterRequest fields
     * If validation fails, MethodArgumentNotValidException is thrown
     * and handled by GlobalExceptionHandler automatically.
     *
     * Returns 201 Created with JWT token on success.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login an existing user.
     *
     * Returns 200 OK with JWT token on success.
     * Returns 401 Unauthorized if credentials are wrong.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
