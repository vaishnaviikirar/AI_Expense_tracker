package com.expensetracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * LoginRequest DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
