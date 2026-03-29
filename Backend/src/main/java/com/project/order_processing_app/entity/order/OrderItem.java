package com.project.order_processing_app.entity.order;

import com.project.order_processing_app.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem — JPA entity mapped to the "order_items" table.
 *
 * Represents one line in an order: a specific product, how many units
 * were ordered, and the price per unit at the time of purchase.
 *
 * ─────────────────────────────────────────────────────────────
 * WHY store price_at_purchase instead of just the product FK?
 * ─────────────────────────────────────────────────────────────
 * Product prices can be updated by admins at any time.
 * If we only stored product_id and queried product.price on reads,
 * the historical order total would silently change whenever
 * an admin updates a price — making order history inaccurate.
 *
 * By storing a price SNAPSHOT at the moment of purchase:
 *   ✓ Historical order values are always accurate
 *   ✓ Customer receipts show what they actually paid
 *   ✓ Refund/dispute calculations are reliable
 *   ✓ Admin can freely update prices without corrupting history
 *
 * This is the standard pattern used by all production e-commerce systems
 * (Shopify, Amazon, Stripe, etc.).
 * ─────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent order. Many items belong to one order.
     * LAZY fetch: we rarely need the full Order when working with items.
     * JoinColumn: creates "order_id" FK column in order_items table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Reference to the product being ordered.
     * Kept for reporting purposes (e.g., "which products are most ordered?").
     * BUT we do NOT use product.price for financial calculations — see priceAtPurchase.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Number of units ordered for this product line */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Price per unit at the exact moment this order was placed.
     * IMMUTABLE — never updated after the order is saved.
     * Line total = priceAtPurchase × quantity.
     */
    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;
}