package com.expensetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * BadRequestException - Thrown for invalid business logic requests.
 * Example: Registering with an already-existing email.
 *
 * Returns HTTP 400 Bad Request
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
