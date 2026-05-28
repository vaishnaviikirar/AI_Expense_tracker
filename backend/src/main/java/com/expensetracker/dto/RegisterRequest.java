package com.expensetracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * RegisterRequest DTO
 *
 * This is the data transfer object for user registration requests.
 * Validation annotations run BEFORE the request reaches the controller method body.
 * If validation fails, Spring throws MethodArgumentNotValidException automatically.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
