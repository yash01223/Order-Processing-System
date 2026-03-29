package com.project.order_processing_app.repository;

import com.project.order_processing_app.entity.Notification;
import com.project.order_processing_app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * NotificationRepository — repository for the notifications table.
 *
 * Phase 2 hook:
 *   countUnreadByUser() will be wrapped with @Cacheable("unread-count") in the service.
 *   @CacheEvict will invalidate it when a notification is created or marked as read.
 *   This avoids running a COUNT query on every GET /notifications page load.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Returns paginated notifications for a user, newest first.
     * Customers see ONLY their own notifications — user scoping is enforced here.
     *
     * Generated SQL:
     *   SELECT * FROM notifications WHERE user_id = ?
     *   ORDER BY created_at DESC LIMIT ? OFFSET ?
     */
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Counts unread notifications for a user.
     * Returned in the list response as "unreadCount" for UI badge display.
     *
     * Uses JPQL instead of derived method name because:
     *   - isRead is a Boolean field (not a primitive boolean)
     *   - JPQL is clearer for this specific boolean check
     *
     * Generated SQL:
     *   SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadByUser(@Param("user") User user);
}