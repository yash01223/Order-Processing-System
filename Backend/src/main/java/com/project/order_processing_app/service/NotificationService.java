package com.project.order_processing_app.service;

import com.project.order_processing_app.dto.response.NotificationResponse;
import com.project.order_processing_app.exception.ResourceNotFoundException;
import com.project.order_processing_app.entity.Notification;
import com.project.order_processing_app.entity.User;
import com.project.order_processing_app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * NotificationService — handles creation and retrieval of notifications.
 *
 * Phase 1: Notifications are written synchronously (direct DB call from OrderService).
 * Phase 2: The createNotification() call in OrderService will be replaced by a
 *          Kafka event. A separate NotificationConsumer bean will call this service.
 *          All READ operations (getNotifications, markAsRead) remain completely unchanged.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Automatic cleanup task: runs every 30 seconds.
     * Deletes any notification that is older than 1 minute.
     */
    @Scheduled(fixedRate = 30*60*1000)
    @Transactional
    public void cleanOldNotifications() {
        LocalDateTime expiryTime = LocalDateTime.now().minusHours(1);
        notificationRepository.deleteByCreatedAtBefore(expiryTime);
    }

    // ═══════════════════════════════════════════════════════════
    //  INTERNAL — called by OrderService after each status change
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a new notification for a user.
     * Called from within OrderService's @Transactional methods.
     *
     * Phase 2: This method will be called by NotificationConsumer instead,
     * after consuming a Kafka order-status-change event.
     *
     * @param user     the customer to notify
     * @param message  the notification text (e.g., "Your order #42 has been dispatched!")
     */
    @Transactional
    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // ═══════════════════════════════════════════════════════════
    //  LIST — GET /api/notifications  [CUSTOMER]
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns paginated notifications for the authenticated customer.
     * Ordered newest-first so the most relevant notifications appear first.
     *
     * Also includes the total unread count so the UI can display a badge.
     *
     * @param user  the authenticated user (from @AuthenticationPrincipal)
     * @param page  zero-based page number
     * @param size  items per page
     * @return      page of NotificationResponse DTOs
     */
    public Page<NotificationResponse> getNotifications(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::toResponse);
    }

    /**
     * Returns the count of unread notifications for the user.
     * Used as a standalone query for the UI badge counter.
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }

    // ═══════════════════════════════════════════════════════════
    //  MARK AS READ — PATCH /api/notifications/{id}/read  [CUSTOMER]
    // ═══════════════════════════════════════════════════════════

    /**
     * Marks a single notification as read.
     *
     * Security check: verifies the notification belongs to the requesting user.
     * Prevents a user from marking another user's notifications as read.
     *
     * @Transactional: the SELECT + UPDATE happen atomically.
     *
     * @throws ResourceNotFoundException  if notification not found
     * @throws SecurityException          if notification belongs to a different user
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        // ── Ownership check — users can only read their own notifications ──
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
            // We throw 404 instead of 403 intentionally:
            // A 403 would confirm the notification exists but belongs to someone else.
            // A 404 reveals nothing about other users' data.
        }

        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    // ═══════════════════════════════════════════════════════════
    //  Private mapper
    // ═══════════════════════════════════════════════════════════

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}