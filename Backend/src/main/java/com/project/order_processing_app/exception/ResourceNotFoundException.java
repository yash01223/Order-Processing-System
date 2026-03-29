package com.project.order_processing_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException — thrown when an entity is not found by ID.
 *
 * @ResponseStatus(404) tells Spring to return HTTP 404 automatically
 * when this exception propagates out of a controller method.
 * Our GlobalExceptionHandler also catches it for a structured JSON error response.
 *
 * Usage examples:
 *   throw new ResourceNotFoundException("Product", "id", 5L);
 *   → "Product not found with id: 5"
 *
 *   throw new ResourceNotFoundException("Order", "id", 99L);
 *   → "Order not found with id: 99"
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName  entity type name (e.g. "Product", "Order", "User")
     * @param fieldName     the field used for lookup (e.g. "id", "email")
     * @param fieldValue    the value that was not found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
