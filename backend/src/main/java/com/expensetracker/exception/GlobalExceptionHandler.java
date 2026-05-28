package com.expensetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized exception handling for the entire application.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * This class catches exceptions thrown ANYWHERE in the application
 * and returns a consistent, clean JSON error response.
 *
 * Without this: Spring returns ugly default error pages.
 * With this: We return clean JSON like:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Email already exists"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Standard error response structure.
     * All errors return this shape so frontend can reliably parse them.
     */
    private Map<String, Object> buildError(int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status);
        error.put("error", message);
        return error;
    }

    /**
     * Handle ResourceNotFoundException (404)
     * Thrown when: expense not found, user not found, etc.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(404, ex.getMessage()));
    }

    /**
     * Handle BadRequestException (400)
     * Thrown when: email already exists, invalid input, etc.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, ex.getMessage()));
    }

    /**
     * Handle Validation Errors (400)
     * Thrown when: @NotBlank, @NotNull, @Positive validations fail.
     * Spring throws MethodArgumentNotValidException automatically.
     *
     * We collect ALL field errors and return them together so the user
     * can fix all problems at once instead of one at a time.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Collect all field errors into a map: {"title": "Title is required", "amount": "..."}
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", 400);
        response.put("error", "Validation failed");
        response.put("details", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle Authentication Errors (401)
     * Thrown when: wrong email/password during login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(buildError(401, "Invalid email or password"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(400, ex.getMessage()));
    }

    /**
     * Handle any other unexpected exceptions (500)
     * Acts as a safety net so we never leak stack traces to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log the actual error for debugging (in production use proper logging)
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(500, "An unexpected error occurred. Please try again."));
    }
}
