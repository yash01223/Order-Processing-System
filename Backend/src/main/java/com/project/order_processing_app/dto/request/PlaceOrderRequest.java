package com.project.order_processing_app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * PlaceOrderRequest — request body for POST /api/orders
 *
 * A customer places an order by sending a list of product + quantity pairs.
 * The server then validates stock, computes the total, and saves the order.
 *
 * @NotEmpty  → list must have at least one item (can't place an empty order)
 * @Valid     → triggers validation on each nested OrderItemRequest object
 *
 * Sample request body:
 * {
 *   "items": [
 *     { "productId": 1, "quantity": 2 },
 *     { "productId": 4, "quantity": 1 }
 *   ]
 * }
 */
@Data
public class PlaceOrderRequest {

    @NotEmpty(message = "Order must contain at least one item")
    @Valid   // ← Cascades validation into each OrderItemRequest
    private List<OrderItemRequest> items;
}