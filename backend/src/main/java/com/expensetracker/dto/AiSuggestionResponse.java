package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AiSuggestionResponse DTO
 *
 * Wraps the AI-generated budget advice string.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiSuggestionResponse {
    private String suggestion;
}
