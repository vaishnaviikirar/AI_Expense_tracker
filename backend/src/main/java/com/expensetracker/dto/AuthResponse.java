package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse DTO
 *
 * Returned after successful login or registration.
 * Contains JWT token + user info so frontend can display user's name, etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String name;
    private Long userId;
}
