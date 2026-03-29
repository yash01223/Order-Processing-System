package com.project.order_processing_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * InsufficientStockException — thrown when a customer tries to order
 * more units of a product than are currently in stock.
 *
 * Thrown inside OrderService.placeOrder() BEFORE any DB writes happen.
 * Because no DB operations have occurred yet, there is nothing to roll back.
 *
 * HTTP 400 Bad Request — the request itself is the problem (not a server error).
 *
 * Usage example:
 *   throw new InsufficientStockException("Laptop", 2, 5);
 *   → "Insufficient stock for product 'Laptop'. Requested: 5, Available: 2"
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    /**
     * @param productName  name of the product with insufficient stock
     * @param available    current stock count in the DB
     * @param requested    quantity the customer tried to order
     */
    public InsufficientStockException(String productName, int available, int requested) {
        super(String.format(
                "Insufficient stock for product '%s'. Requested: %d, Available: %d",
                productName, requested, available
        ));
    }
}