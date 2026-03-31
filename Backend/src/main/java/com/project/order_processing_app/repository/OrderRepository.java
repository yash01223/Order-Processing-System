package com.project.order_processing_app.repository;

import com.project.order_processing_app.entity.order.Order;
import com.project.order_processing_app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = { "user" })
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT COUNT(o) FROM Order o")
    long countAllOrders();

    /**
     * Total revenue = sum of total_amount for all DELIVERED orders.
     * COALESCE(..., 0) returns 0 instead of NULL when no orders exist.
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal sumRevenue();

    /** Count of orders currently waiting to be confirmed by admin */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    long countPending();

    /** Count of orders that reached the terminal DELIVERED state */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED'")
    long countDelivered();

    /**
     * Finds orders that are Delivered or Cancelled and whose status was last updated
     * before the provided timestamp. The service layer calls deleteAll() on the result
     * to trigger JPA cascade (removing order_items and order_status_history).
     */
    @Query("SELECT o FROM Order o WHERE o.status IN (com.project.order_processing_app.entity.order.OrderStatus.DELIVERED, com.project.order_processing_app.entity.order.OrderStatus.CANCELLED) AND o.statusUpdatedAt < :timestamp")
    List<Order> findExpiredOrders(@Param("timestamp") LocalDateTime timestamp);
}
