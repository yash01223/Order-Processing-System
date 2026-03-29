package com.project.order_processing_app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification — JPA entity mapped to the "notifications" table.
 *
 * A notification is created for the customer on:
 *   1. Order placement (PENDING)
 *   2. Every status transition (CONFIRMED, DISPATCHED, DELIVERED, CANCELLED)
 *
 * Phase 1 — Synchronous write:
 *   OrderService writes a Notification directly to the DB inside the same
 *   @Transactional block as the status change. Simple and easy to verify.
 *
 * Phase 2 — Async via Kafka:
 *   The direct DB write will be replaced by a Kafka event publish.
 *   A separate NotificationConsumer bean will listen and write the DB record.
 *   IMPORTANT: The GET /notifications and PATCH /notifications/{id}/read
 *   endpoints remain 100% unchanged — only the write path moves to Kafka.
 *
 * is_read lifecycle:
 *   Created as false (unread).
 *   Set to true via PATCH /notifications/{id}/read.
 *   Used to show an unread badge count in the UI.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user this notification is addressed to.
     * LAZY: we rarely need the full User object when listing notifications.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Human-readable message shown to the customer.
     * e.g. "Your order #42 has been dispatched and is on the way!"
     * TEXT type allows longer messages without the 255-char VARCHAR limit.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Has the customer acknowledged/read this notification?
     * Default: false (unread). @Builder.Default ensures Lombok @Builder
     * sets this to false even when building — not null.
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}