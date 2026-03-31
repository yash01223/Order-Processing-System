package com.project.order_processing_app.entity.product;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Product — JPA entity mapped to the "products" table.
 *
 * Key design decisions:
 *
 *   BigDecimal for price:
 *     Never use double or float for monetary values. Floating-point arithmetic
 *     is imprecise — e.g., 0.1 + 0.2 = 0.30000000000000004 in IEEE 754.
 *     BigDecimal guarantees exact decimal arithmetic for financial calculations.
 *
 *   stock_count management:
 *     Decremented atomically inside @Transactional when an order is placed.
 *     Incremented atomically inside @Transactional when an order is cancelled.
 *     We always check stock_count >= requested quantity BEFORE saving the order.
 *     If insufficient, InsufficientStockException is thrown and nothing is saved.
 *
 *   category:
 *     Free-form VARCHAR for Phase 1. In a later iteration this could become
 *     a FK reference to a separate categories table for structured filtering.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /**
     * columnDefinition = "TEXT" → PostgreSQL TEXT type (unlimited length).
     * Default VARCHAR maps to VARCHAR(255) which may be too short for descriptions.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * DECIMAL(10, 2) → up to 99,999,999.99
     * precision = total digits, scale = digits after decimal point.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Available inventory count.
     * Modified only inside @Transactional service methods.
     * Never goes below 0 — InsufficientStockException prevents overselling.
     */
    @Column(name = "stock_count", nullable = false)
    private Integer stockCount;

    /** Free-text category label, e.g. "Electronics", "Clothing", "Books" */
    @Column
    private String category;

}