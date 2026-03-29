package com.project.order_processing_app.controller;

import com.project.order_processing_app.dto.response.NotificationResponse;
import com.project.order_processing_app.entity.User;
import com.project.order_processing_app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NotificationController — customer notification endpoints.
 *
 * Base path: /api/notifications
 *
 * Access rules: CUSTOMER only — customers see only their own notifications.
 *
 * Phase 2 note:
 *   These endpoints remain COMPLETELY UNCHANGED when Kafka is added.
 *   Only the write path (how notifications are created) moves to Kafka consumer.
 *   All read and mark-as-read operations stay exactly as they are.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ─────────────────────────────────────────────────────────
    //  GET /api/notifications  [CUSTOMER]
    // ─────────────────────────────────────────────────────────

    /**
     * Returns paginated notifications for the authenticated customer.
     * Ordered newest-first. Also returns the total unread count.
     *
     * Postman test:
     *   GET http://localhost:8080/api/notifications?page=0&size=10
     *   Authorization: Bearer <customer-token>
     *
     * Expected: 200 with:
     *   {
     *     "notifications": { ...paginated Page<NotificationResponse>... },
     *     "unreadCount": 3
     *   }
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificationResponse> notifications =
                notificationService.getNotifications(currentUser, page, size);

        long unreadCount = notificationService.getUnreadCount(currentUser);

        // Return both the page of notifications AND the unread badge count
        // Using Map<String, Object> for a flexible combined response
        return ResponseEntity.ok(Map.of(
                "notifications", notifications,
                "unreadCount",   unreadCount
        ));
    }

    // ─────────────────────────────────────────────────────────
    //  PATCH /api/notifications/{id}/read  [CUSTOMER]
    // ─────────────────────────────────────────────────────────

    /**
     * Marks a specific notification as read.
     *
     * Ownership is enforced in NotificationService.markAsRead() —
     * returns 404 if the notification belongs to a different user.
     *
     * Postman test:
     *   PATCH http://localhost:8080/api/notifications/5/read
     *   Authorization: Bearer <customer-token>
     *   (no body required)
     *
     * Expected: 200 with updated notification (isRead = true)
     * Error:    404 if notification not found or belongs to another user
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(id, currentUser));
    }
}
