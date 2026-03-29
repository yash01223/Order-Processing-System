package com.project.order_processing_app.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * ProductRequest — request body for POST /api/products and PUT /api/products/{id}
 *
 * Admin-only endpoint. Validated before reaching ProductService.
 *
 * Sample request body:
 * {
 *   "name": "Gaming Laptop",
 *   "description": "High-performance laptop with RTX 4060",
 *   "price": 89999.99,
 *   "stockCount": 50,
 *   "category": "Electronics"
 * }
 */
@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    // description is optional — no @NotBlank
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock count is required")
    @Min(value = 0, message = "Stock count cannot be negative")
    private Integer stockCount;

    // category is optional
    private String category;
}