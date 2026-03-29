package com.project.order_processing_app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OrderItemRequest — one line item inside a PlaceOrderRequest.
 *
 * Specifies which product and how many units the customer wants.
 *
 * Sample:
 * {
 *   "productId": 3,
 *   "quantity": 2
 * }
 */
@Data
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
