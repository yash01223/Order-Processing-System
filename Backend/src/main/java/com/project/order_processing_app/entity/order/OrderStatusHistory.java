package com.project.order_processing_app.entity.order;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OrderStatusHistory — JPA entity mapped to "order_status_history" table.
 *
 * Records every status transition an order goes through.
 * This table is APPEND-ONLY — rows are never updated or deleted.
 * It forms a complete, immutable audit trail.
 *
 * Example history for order #42:
 *   id=1  | PENDING    → CONFIRMED   | 2024-01-10 09:00
 *   id=2  | CONFIRMED  → DISPATCHED  | 2024-01-11 14:30
 *   id=3  | DISPATCHED → DELIVERED   | 2024-01-12 17:00
 *
 * Used for:
 *   - Customer queries ("when did my order ship?")
 *   - Admin reporting and dispute resolution
 *   - Audit compliance
 *   - Phase 2: Kafka event replay uses this as the source of truth
 *
 * A record is written by OrderService every time status changes,
 * inside the same @Transactional block — so if the status update
 * fails, the history record is also rolled back (atomicity guaranteed).
 */
@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The order this history entry belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** Status BEFORE this transition */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", nullable = false)
    private OrderStatus oldStatus;

    /** Status AFTER this transition */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private OrderStatus newStatus;

    /** Timestamp when the transition occurred — set once, never updated */
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}