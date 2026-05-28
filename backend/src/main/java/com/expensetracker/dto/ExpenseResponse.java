package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ExpenseResponse DTO
 *
 * What we return when a client requests expense data.
 * We NEVER return the full entity (which has the User object with password etc.)
 * DTOs are a security best practice — expose only what's needed.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDate date;
}
