package com.expensetracker.controller;

import com.expensetracker.dto.AiSuggestionResponse;
import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.OpenAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ExpenseController - Handles all expense-related HTTP requests.
 *
 * ALL endpoints here are SECURED — require a valid JWT token.
 *
 * Key concept: @AuthenticationPrincipal UserDetails userDetails
 * Spring Security automatically injects the authenticated user's details.
 * We use userDetails.getUsername() to get the email from the JWT.
 * This means users can ONLY access their own data — never other users' data!
 *
 * API Endpoints:
 * POST   /api/expenses           → Add a new expense
 * GET    /api/expenses           → Get all expenses for logged-in user
 * DELETE /api/expenses/{id}      → Delete a specific expense
 * GET    /api/expenses/ai-suggestion → Get AI budget advice
 */
@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final OpenAIService openAIService;

    /**
     * Add a new expense.
     *
     * The logged-in user is determined by the JWT token, NOT by any request parameter.
     * This prevents users from adding expenses for other users.
     */
    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(
            @Valid @RequestBody ExpenseRequest request,
            @AuthenticationPrincipal UserDetails userDetails  // Injected from JWT
    ) {
        String userEmail = userDetails.getUsername(); // Email stored in JWT
        ExpenseResponse response = expenseService.addExpense(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all expenses for the authenticated user.
     * Returns empty list if user has no expenses.
     */
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        List<ExpenseResponse> expenses = expenseService.getUserExpenses(userEmail);
        return ResponseEntity.ok(expenses);
    }

    /**
     * Delete a specific expense.
     *
     * Security: Even though the ID is in the URL, we still verify
     * the expense belongs to the authenticated user in the service layer.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        expenseService.deleteExpense(id, userEmail);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Get AI budget suggestions based on expense data.
     *
     * Flow:
     * 1. Get category-wise summary for this user
     * 2. Send to OpenAI
     * 3. Return AI-generated advice
     */
    @GetMapping("/ai-suggestion")
    public ResponseEntity<AiSuggestionResponse> getAiSuggestion(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();

        // Step 1: Get expense summary
        String expenseSummary = expenseService.getCategoryWiseSummary(userEmail);

        // Step 2: Generate AI advice
        String suggestion = openAIService.generateBudgetAdvice(expenseSummary);

        // Step 3: Return response
        return ResponseEntity.ok(new AiSuggestionResponse(suggestion));
    }
}
