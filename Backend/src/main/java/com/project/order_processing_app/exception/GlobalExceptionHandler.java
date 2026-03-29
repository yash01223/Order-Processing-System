package com.project.order_processing_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — centralized exception handling for all controllers.
 *
 * @RestControllerAdvice intercepts exceptions thrown from ANY @RestController
 * and converts them into structured JSON error responses instead of the default
 * Spring "Whitelabel Error Page" or raw stack trace.
 *
 * Without this class, exceptions propagate and Spring returns either:
 *   - 500 Internal Server Error with a generic message
 *   - An HTML error page (useless for API clients)
 *
 * With this class, every exception returns a consistent JSON envelope:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Insufficient stock for product 'Laptop'. Requested: 5, Available: 2"
 * }
 *
 * For validation errors, the response includes per-field error details:
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Input validation failed",
 *   "fieldErrors": {
 *     "email": "Must be a valid email address",
 *     "password": "Password must be at least 6 characters"
 *   }
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════════
    //  Helper — builds the standard error response map
    // ═══════════════════════════════════════════════════════════

    private Map<String, Object> buildError(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    // ═══════════════════════════════════════════════════════════
    //  404 — Resource Not Found
    // ═══════════════════════════════════════════════════════════

    /**
     * Handles ResourceNotFoundException.
     * Triggered when a product, order, user, or notification ID doesn't exist.
     * Returns HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    // ═══════════════════════════════════════════════════════════
    //  400 — Business Logic Violations
    // ═══════════════════════════════════════════════════════════

    /**
     * Handles InsufficientStockException.
     * Triggered when a customer tries to order more than the available stock.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Insufficient Stock", ex.getMessage()));
    }

    /**
     * Handles OrderCancellationException.
     * Triggered when a cancellation is attempted on a non-PENDING order.
     */
    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCancellation(OrderCancellationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, "Cancellation Not Allowed", ex.getMessage()));
    }

    // ═══════════════════════════════════════════════════════════
    //  400 — Input Validation Failures
    // ═══════════════════════════════════════════════════════════

    /**
     * Handles @Valid validation failures on @RequestBody DTOs.
     *
     * When a request body fails validation (e.g., missing email, weak password),
     * Spring throws MethodArgumentNotValidException with a list of FieldErrors.
     * This handler extracts each field + its error message into a clean map.
     *
     * Example response:
     * {
     *   "fieldErrors": {
     *     "email": "Must be a valid email address",
     *     "password": "Password must be at least 6 characters"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Collect per-field error messages
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = buildError(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Input validation failed"
        );
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ═══════════════════════════════════════════════════════════
    //  409 — Conflict
    // ═══════════════════════════════════════════════════════════

    /**
     * Handles DuplicateEmailException.
     * Triggered when a registration attempt uses an already-registered email.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, "Conflict", ex.getMessage()));
    }

    // ═══════════════════════════════════════════════════════════
    //  401 — Authentication Failure
    // ═══════════════════════════════════════════════════════════

    /**
     * Handles BadCredentialsException.
     * Triggered by Spring Security when login email/password don't match.
     * Returns a generic "Invalid credentials" message to avoid revealing
     * whether the email or password was wrong (security best practice).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(buildError(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid email or password"));
    }

    // ═══════════════════════════════════════════════════════════
    //  403 — Authorization Failure
    // ═══════════════════════════════════════════════════════════

    /**
     * Handles AccessDeniedException.
     * Triggered when a CUSTOMER tries to access an ADMIN-only endpoint
     * (e.g., POST /api/products or GET /api/admin/dashboard).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(buildError(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource"));
    }

    // ═══════════════════════════════════════════════════════════
    //  500 — Catch-all for unexpected errors
    // ═══════════════════════════════════════════════════════════

    /**
     * Catches any exception not handled by the above handlers.
     * Logs a generic 500 response — the real error is in the server logs.
     * Never expose stack traces or internal details to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log internally (in production use a proper logger like SLF4J)
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred. Please try again later."
                ));
    }
}