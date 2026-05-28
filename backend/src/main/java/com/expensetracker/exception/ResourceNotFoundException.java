package com.expensetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException - Thrown when a requested resource doesn't exist.
 * Example: GET /api/expenses/999 where expense 999 doesn't exist.
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND) → Returns 404 automatically
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
