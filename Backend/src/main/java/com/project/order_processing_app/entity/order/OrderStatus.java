//package com.project.order_processing_app.entity;
package com.project.order_processing_app.entity.order;

/**
 * OrderStatus — every possible state in the order pipeline.
 *
 * Allowed transitions (enforced in OrderService):
 *
 *   PENDING ──► CONFIRMED ──► DISPATCHED ──► DELIVERED
 *      │
 *      └──► CANCELLED  (customer only; only allowed from PENDING)
 *
 * Rules:
 *   - Only an ADMIN can advance an order through the main pipeline.
 *   - Only the owning CUSTOMER can cancel, and only while PENDING.
 *   - Every transition is recorded in order_status_history for a full audit trail.
 *   - Stock is DECREMENTED when an order is placed (goes to PENDING).
 *   - Stock is RESTORED when an order is CANCELLED.
 *
 * Stored as STRING in DB for readability and forward-compatibility.
 */
public enum OrderStatus {

    /**
     * Set immediately when a customer places an order.
     * Stock is decremented at this point.
     * Customer can still cancel from this state.
     */
    PENDING,

    /**
     * Admin has reviewed and confirmed the order.
     * Customer can NO longer cancel from this point.
     */
    CONFIRMED,

    /**
     * Order has been handed to the logistics/shipping service.
     */
    DISPATCHED,

    /**
     * Order successfully delivered to the customer.
     * Terminal success state — no further transitions allowed.
     */
    DELIVERED,

    /**
     * Customer cancelled the order while it was still PENDING.
     * Stock is restored in the same @Transactional block.
     * Terminal cancelled state — no further transitions allowed.
     */
    CANCELLED
}