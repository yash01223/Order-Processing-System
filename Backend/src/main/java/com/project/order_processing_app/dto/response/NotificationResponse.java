package com.project.order_processing_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationResponse — response for GET /api/notifications
 *
 * Sample response:
 * {
 *   "id": 7,
 *   "message": "Your order #42 has been dispatched and is on the way!",
 *   "isRead": false,
 *   "createdAt": "2024-01-11T14:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}