package com.project.order_processing_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DashboardResponse — response for GET /api/admin/dashboard
 *
 * All values are computed by a single JPQL aggregation query in OrderRepository.
 * Phase 2: DashboardService.getStats() will be annotated with @Cacheable("dashboard")
 * so this expensive aggregation is served from Redis on repeated calls.
 *
 * Sample response:
 * {
 *   "totalOrders": 150,
 *   "totalRevenue": 1250000.00,
 *   "pendingOrders": 12,
 *   "deliveredOrders": 98
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long deliveredOrders;
}