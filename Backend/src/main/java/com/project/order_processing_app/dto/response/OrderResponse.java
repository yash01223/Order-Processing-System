package com.project.order_processing_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderResponse — outgoing order data for GET /api/orders and GET
 * /api/orders/{id}
 *
 * The list endpoint (GET /api/orders) returns orders WITHOUT items (items =
 * null)
 * for performance — no need to load all line items for a summary list.
 *
 * The detail endpoint (GET /api/orders/{id}) populates the items list
 * using the JOIN FETCH query in OrderRepository.
 *
 * Sample response (detail):
 * {
 * "id": 42,
 * "userId": 1,
 * "customerName": "Yash Sangale",
 * "status": "DISPATCHED",
 * "totalAmount": 92999.97,
 * "createdAt": "2024-01-10T09:00:00",
 * "items": [
 * { "productId": 1, "productName": "Gaming Laptop", "quantity": 1,
 * "priceAtPurchase": 89999.99, "lineTotal": 89999.99 },
 * { "productId": 3, "productName": "Mouse", "quantity": 3, "priceAtPurchase":
 * 999.99, "lineTotal": 2999.97 }
 * ]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime statusUpdatedAt;

    /**
     * Null for list responses, populated for detail responses.
     * Avoids loading all items on every list page load.
     */
    private List<OrderItemResponse> items;
}