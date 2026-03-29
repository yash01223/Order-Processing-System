package com.project.order_processing_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * DuplicateEmailException — thrown when a registration attempt uses
 * an email address that is already registered.
 *
 * HTTP 409 Conflict — the resource (email) already exists.
 *
 * Usage:
 *   throw new DuplicateEmailException("yash@example.com");
 *   → "An account with email 'yash@example.com' already exists"
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super(String.format("An account with email '%s' already exists", email));
    }
}