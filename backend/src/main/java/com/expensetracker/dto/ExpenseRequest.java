package com.expensetracker.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ExpenseRequest DTO
 *
 * Used when creating a new expense.
 * @NotBlank  = field must not be null AND must not be empty/whitespace
 * @NotNull   = field must not be null (for non-String types)
 * @Positive  = number must be greater than 0
 */
@Data
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be a positive number")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;
}
