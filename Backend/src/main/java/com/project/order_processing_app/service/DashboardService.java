package com.project.order_processing_app.service;

import com.project.order_processing_app.dto.response.DashboardResponse;
import com.project.order_processing_app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * DashboardService — aggregated statistics for the admin dashboard.
 *
 * Uses four separate JPQL COUNT/SUM queries from OrderRepository.
 * Each query runs a single DB aggregation — no data is loaded into Java memory.
 *
 * Phase 2 hook:
 *   Add @Cacheable("dashboard") to getStats() — the four DB queries will then
 *   only run when the cache is cold or invalidated.
 *   Add @CacheEvict(value="dashboard", allEntries=true) to OrderService.advanceOrderStatus()
 *   and cancelOrder() so stats refresh whenever an order changes.
 *
 *   Without caching: every GET /admin/dashboard runs 4 aggregation queries.
 *   With Redis:      served from memory in <1ms until a status change occurs.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;

    // ═══════════════════════════════════════════════════════════
    //  GET STATS — GET /api/admin/dashboard  [ADMIN]
    // ═══════════════════════════════════════════════════════════

    /**
     * Computes and returns aggregated order statistics.
     *
     * Phase 2 hook: add @Cacheable("dashboard") here.
     * Phase 2 hook: add @CacheEvict on OrderService write methods.
     *
     * @return DashboardResponse with totalOrders, totalRevenue, pendingOrders, deliveredOrders
     */
    // Phase 2: @Cacheable("dashboard")
    public DashboardResponse getStats() {
        return DashboardResponse.builder()
                // Total count of all orders regardless of status
                .totalOrders(orderRepository.countAllOrders())
                // Sum of totalAmount for DELIVERED orders only
                .totalRevenue(orderRepository.sumRevenue())
                // Orders waiting to be confirmed
                .pendingOrders(orderRepository.countPending())
                // Successfully delivered orders
                .deliveredOrders(orderRepository.countDelivered())
                .build();
    }
}