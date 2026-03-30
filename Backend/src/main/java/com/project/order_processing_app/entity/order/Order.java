package com.project.order_processing_app.entity.order;

import com.project.order_processing_app.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order — JPA entity mapped to the "orders" table.
 *
 * Represents a single customer purchase transaction.
 * One Order contains 1..N OrderItems (the individual product lines).
 *
 * Table name: "orders" not "order" — ORDER is a reserved SQL keyword.
 *
 * Key design decisions:
 *
 *   total_amount stored as snapshot:
 *     Computed once at placement time from sum(price_at_purchase × quantity).
 *     Stored on the order so historical totals are accurate even if product
 *     prices change later. Never recalculated on reads.
 *
 *   CascadeType.ALL on items:
 *     Saving an Order automatically saves all its OrderItems.
 *     Deleting an Order automatically deletes all its OrderItems.
 *     We never need to call orderItemRepository.save() directly.
 *
 *   orphanRemoval = true:
 *     If an item is removed from the items list in Java,
 *     Hibernate deletes the corresponding DB row automatically.
 *
 *   FetchType.LAZY on user:
 *     User data is NOT loaded from DB unless explicitly accessed.
 *     Most order queries don't need the full User object — this avoids
 *     unnecessary JOINs and improves performance.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The customer who placed this order.
     * FetchType.LAZY: loaded only when order.getUser() is called explicitly.
     * JoinColumn: creates column "user_id" (FK → users.id) in the orders table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The line items (product + quantity + price) in this order.
     *
     * mappedBy = "order": the FK relationship is owned by OrderItem.order,
     * not by this list. Hibernate uses OrderItem's @JoinColumn to generate SQL.
     *
     * @Builder.Default: Lombok @Builder sets this to new ArrayList() by default
     * so builder().build() doesn't produce a null list.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Cascading: Deleting an Order deletes its entire audit trail.
     * mappedBy points to the "order" field in the OrderStatusHistory entity.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderStatusHistory> history = new ArrayList<>();

    /**
     * Current status in the order pipeline.
     * Defaults to PENDING when a new order is placed.
     * Transitions are validated and enforced in OrderService.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Total order value — computed and stored at placement time.
     * = sum(item.priceAtPurchase × item.quantity) for all items.
     * IMMUTABLE after creation.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** Record creation timestamp — set once, never modified */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last status update timestamp.
     * Used for the 5-minute automatic deletion task (Delivered/Cancelled).
     */
    @Column(name = "status_updated_at", nullable = false)
    private LocalDateTime statusUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.statusUpdatedAt = LocalDateTime.now();
    }
}