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


@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0")  int page,   
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<NotificationResponse> notifications =
                notificationService.getNotifications(currentUser, page, size);

        // long unreadCount = notificationService.getUnreadCount(currentUser);

        return ResponseEntity.ok(Map.of(
                "notifications", notifications
                // "unreadCount",   unreadCount
        ));
    }

  
//     @PatchMapping("/{id}/read")
//     @PreAuthorize("hasRole('CUSTOMER')")
//     public ResponseEntity<NotificationResponse> markAsRead(
//             @PathVariable Long id,
//             @AuthenticationPrincipal User currentUser
//     ) {
//         return ResponseEntity.ok(notificationService.markAsRead(id, currentUser));
//     }
}
