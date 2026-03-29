package com.project.order_processing_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * OrderCancellationException — thrown when a cancellation attempt is invalid.
 *
 * Reasons a cancellation can fail:
 *   1. The order is not in PENDING state (e.g., already CONFIRMED or DISPATCHED)
 *   2. The requesting user does not own the order
 *
 * HTTP 400 Bad Request — the client sent a logically invalid operation.
 *
 * Usage examples:
 *   throw new OrderCancellationException("Order can only be cancelled when in PENDING status. Current status: CONFIRMED");
 *   throw new OrderCancellationException("You are not authorized to cancel this order");
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OrderCancellationException extends RuntimeException {

    public OrderCancellationException(String message) {
        super(message);
    }
}