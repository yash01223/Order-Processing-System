package com.project.order_processing_app.repository;

import com.project.order_processing_app.entity.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderStatusHistoryRepository — append-only audit log for order transitions.
 *
 * This table is WRITE-HEAVY, READ-LIGHT in normal operations.
 * Every status change writes one new row — rows are never updated or deleted.
 *
 * Reads are used in:
 *   - Customer support tooling ("show me the full history of order #42")
 *   - Admin audit views
 *   - Phase 2: Kafka AuditConsumer will also write here asynchronously
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    /**
     * Retrieves the full transition history for one order, newest first.
     *
     * Spring Data derives query from method name:
     *   findBy OrderId         → WHERE order_id = ?
     *   OrderBy ChangedAt Desc → ORDER BY changed_at DESC
     *
     * Generated SQL:
     *   SELECT * FROM order_status_history
     *   WHERE order_id = ?
     *   ORDER BY changed_at DESC
     */
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
}