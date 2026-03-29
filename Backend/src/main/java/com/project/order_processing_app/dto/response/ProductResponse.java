package com.project.order_processing_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ProductResponse — outgoing product data for all product endpoints.
 *
 * WHY use a separate response DTO instead of returning the entity directly?
 *   - Prevents accidental exposure of internal fields (e.g., if we add an
 *     internal cost price field, it stays out of the response automatically)
 *   - Decouples the API contract from the DB schema
 *   - Allows adding computed fields (e.g., "inStock") without changing the entity
 *
 * Sample response:
 * {
 *   "id": 1,
 *   "name": "Gaming Laptop",
 *   "description": "High-performance laptop with RTX 4060",
 *   "price": 89999.99,
 *   "stockCount": 48,
 *   "category": "Electronics",
 *   "inStock": true
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockCount;
    private String category;

    /** Computed convenience field: true if stockCount > 0 */
    private boolean inStock;
}