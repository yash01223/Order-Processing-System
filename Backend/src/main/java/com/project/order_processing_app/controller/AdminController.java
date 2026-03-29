package com.project.order_processing_app.controller;

import com.project.order_processing_app.dto.response.DashboardResponse;
import com.project.order_processing_app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AdminController — admin-only management endpoints.
 *
 * Base path: /api/admin
 * All endpoints require ADMIN role — enforced by @PreAuthorize at class/method
 * level.
 *
 * Product management (POST/PUT/DELETE /products) lives in ProductController
 * with its own @PreAuthorize guards, keeping related CRUD together.
 *
 * Order status advancement (PATCH /orders/{id}/status) lives in
 * OrderController.
 *
 * This controller focuses on analytics and aggregate views.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Applies ADMIN guard to ALL methods in this controller
public class AdminController {

    private final DashboardService dashboardService;

    /**
     * Returns aggregated order statistics for the admin dashboard.
     *
     * Runs 4 JPQL aggregation queries (COUNT + SUM) on the orders table.
     * Phase 2: DashboardService.getStats() gets @Cacheable("dashboard") —
     * results served from Redis in <1ms between order status changes.
     *
     * Postman test:
     * GET http://localhost:8080/api/admin/dashboard
     * Authorization: Bearer <admin-token>
     *
     * Expected response:
     * {
     * "totalOrders": 150,
     * "totalRevenue": 1250000.00,
     * "pendingOrders": 12,
     * "deliveredOrders": 98
     * }
     *
     * Error: 403 Forbidden if called with a CUSTOMER token
     */

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getStats());
    }
}