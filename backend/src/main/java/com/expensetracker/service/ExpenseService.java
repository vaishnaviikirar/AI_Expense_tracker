package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ExpenseService - Business logic for expense management.
 *
 * Key pattern: mapToResponse() converts Entity → DTO
 * This ensures we never expose raw database entities to the client.
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    /**
     * Add a new expense for a user.
     *
     * @param request  - Expense data from frontend
     * @param userEmail - Email extracted from JWT token (trusted)
     * @return ExpenseResponse DTO
     */
    public ExpenseResponse addExpense(ExpenseRequest request, String userEmail) {
        // Find the user (we trust the email came from JWT)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Create expense entity
        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setUser(user);  // Link expense to the authenticated user

        // Save to database
        Expense saved = expenseRepository.save(expense);

        // Return DTO (never return raw entity)
        return mapToResponse(saved);
    }

    /**
     * Get all expenses for the authenticated user.
     *
     * @param userEmail - Email extracted from JWT token
     * @return List of ExpenseResponse DTOs
     */
    public List<ExpenseResponse> getUserExpenses(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Fetch all expenses for this user (ordered by date desc = newest first)
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(user.getId());

        // Convert each Entity to DTO using Java Stream API
        return expenses.stream()
                .map(this::mapToResponse)   // mapToResponse is called for each expense
                .collect(Collectors.toList());
    }

    /**
     * Delete a specific expense by ID.
     *
     * Security check: We verify the expense belongs to the requesting user.
     * Without this check, any authenticated user could delete any expense!
     *
     * @param expenseId - ID of expense to delete
     * @param userEmail - Email of authenticated user (from JWT)
     */
    public void deleteExpense(Long expenseId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Security check: find expense by BOTH ID and user ID
        // If this expense belongs to a different user, it won't be found → 404
        Expense expense = expenseRepository.findByIdAndUserId(expenseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found with id: " + expenseId));

        expenseRepository.delete(expense);
    }

    /**
     * Get category-wise expense totals for AI analysis.
     * Returns a map of category → total amount as a formatted string.
     *
     * @param userEmail - Email of authenticated user
     * @return Formatted string summary of expenses by category
     */
    public String getCategoryWiseSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        List<Object[]> categoryTotals = expenseRepository.findCategoryWiseTotalByUserId(user.getId());

        if (categoryTotals.isEmpty()) {
            return "No expenses found.";
        }

        // Build a human-readable summary for the AI prompt
        StringBuilder summary = new StringBuilder();
        summary.append("User's expense breakdown by category:\n");

        for (Object[] row : categoryTotals) {
            String category = (String) row[0];
            java.math.BigDecimal total = (java.math.BigDecimal) row[1];
            summary.append(String.format("- %s: ₹%.2f\n", category, total));
        }

        return summary.toString();
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    /**
     * Convert an Expense entity to an ExpenseResponse DTO.
     * This is the mapper — Entity → DTO transformation.
     */
    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate()
        );
    }
}
