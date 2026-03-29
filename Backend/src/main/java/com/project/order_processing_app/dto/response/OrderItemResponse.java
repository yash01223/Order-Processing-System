package com.project.order_processing_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItemResponse — one line item inside an OrderResponse.
 *
 * Shows the product snapshot as it was at purchase time.
 * priceAtPurchase reflects what the customer actually paid — not the current product price.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;     // Fetched from the product FK at mapping time
    private Integer quantity;
    private BigDecimal priceAtPurchase;   // Historical price snapshot
    private BigDecimal lineTotal;         // Computed: priceAtPurchase × quantity
}
