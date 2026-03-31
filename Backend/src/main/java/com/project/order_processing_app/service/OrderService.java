package com.project.order_processing_app.service;

import com.project.order_processing_app.dto.request.OrderItemRequest;
import com.project.order_processing_app.dto.request.PlaceOrderRequest;
import com.project.order_processing_app.dto.response.OrderItemResponse;
import com.project.order_processing_app.dto.response.OrderResponse;
import com.project.order_processing_app.entity.order.Order;
import com.project.order_processing_app.entity.order.OrderItem;
import com.project.order_processing_app.entity.order.OrderStatusHistory;
import com.project.order_processing_app.entity.product.Product;
import com.project.order_processing_app.exception.InsufficientStockException;
import com.project.order_processing_app.exception.OrderCancellationException;
import com.project.order_processing_app.exception.ResourceNotFoundException;
import com.project.order_processing_app.entity.*;
import com.project.order_processing_app.entity.order.OrderStatus;
import com.project.order_processing_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final NotificationService notificationService;

    /**
     * Automatic cleanup task: runs every 60 seconds.
     * Deletes orders that are Delivered or Cancelled and whose status was last updated
     * more than 5 minutes ago.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void purgeCompletedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<Order> expiredOrders = orderRepository.findExpiredOrders(threshold);
        if (!expiredOrders.isEmpty()) {
            orderRepository.deleteAll(expiredOrders);
        }
    }

    
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request, User currentUser) {

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // ── Step 1: Validate stock for every item BEFORE writing anything ──
        // We check all items first so we don't partially decrement stock
        // before discovering that a later item is out of stock.
        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemRequest.getProductId()));

            // Guard: is enough stock available?
            if (product.getStockCount() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(),
                        product.getStockCount(),
                        itemRequest.getQuantity());
            }

            // Build the order item (not yet saved — order doesn't exist yet)
            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    // SNAPSHOT: capture current price — survives future price changes
                    .priceAtPurchase(product.getPrice())
                    .build();

            orderItems.add(item);

            // Running total: priceAtPurchase × quantity
            totalAmount = totalAmount.add(
                    product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        // ── Step 2: Save the Order entity ──────────────────────────────
        Order order = Order.builder()
                .user(currentUser)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .statusUpdatedAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // ── Step 3: Attach items to the saved order and persist ─────────
        // We must set order on each item AFTER the order has an ID (savedOrder.getId())
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        savedOrder.getItems().addAll(orderItems);
        // CascadeType.ALL saves all OrderItems automatically when the order is saved
        orderRepository.save(savedOrder);

        // ── Step 4: Decrement stock for each product ────────────────────
        // Done AFTER validating ALL items — no partial decrements if validation fails
        for (OrderItem item : savedOrder.getItems()) {
            Product product = item.getProduct();
            product.setStockCount(product.getStockCount() - item.getQuantity());
            productRepository.save(product);
        }

        // ── Step 5: Write initial status history record ─────────────────
        // For order placement, oldStatus and newStatus are both PENDING
        // (this is the "order created" entry in the audit trail)
        writeStatusHistory(savedOrder, OrderStatus.PENDING, OrderStatus.PENDING);

        // ── Step 6: Notify the customer ─────────────────────────────────
        String productNames = savedOrder.getItems().stream()
                .map(item -> item.getProduct().getName())
                .collect(Collectors.joining(", "));
        
        notificationService.createNotification(
                currentUser,
                String.format("Your order for [%s] has been placed successfully!", productNames)
        );

        return toResponse(savedOrder, true);
    }

    // ═══════════════════════════════════════════════════════════
    // LIST ORDERS — GET /api/orders [CUSTOMER]
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns paginated orders for the current customer.
     * Does NOT include item details — just the order summary.
     * Use GET /orders/{id} for full details with items.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(User currentUser, int page, int size) {
        if (currentUser == null) {
            throw new RuntimeException("Authenticated user is null in OrderService!");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            // Use string comparison for role to avoid proxy/classloader issues
            if ("ADMIN".equals(currentUser.getRole().name())) {
                return orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(order -> toResponse(order, false));
            }
            return orderRepository
                    .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                    .map(order -> toResponse(order, false)); // false = don't include items
        } catch (Exception e) {
            // Write error to a file so I can debug it
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Paths.get("backend_error.log"),
                        "Error in getMyOrders: " + e.getMessage() + "\n" +
                                java.util.Arrays.toString(e.getStackTrace()),
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // GET ORDER DETAIL — GET /api/orders/{id} [CUSTOMER]
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns full order detail including all line items.
     * Uses JOIN FETCH query to load items + products in a single SQL call.
     *
     * Security: verifies the order belongs to the requesting customer.
     *
     * @throws ResourceNotFoundException if order not found or belongs to another
     *                                   user
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, User currentUser) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Ownership check: customers can only view their own orders
        if (currentUser.getRole() != Role.ADMIN && !order.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Order", "id", orderId);
            // Return 404 (not 403) to avoid revealing that a different user's order exists
        }

        return toResponse(order, true); // true = include items in response
    }

    // ═══════════════════════════════════════════════════════════
    // ADVANCE STATUS — PATCH /api/orders/{id}/status [ADMIN]
    // ═══════════════════════════════════════════════════════════

    /**
     * Advances the order to the next stage in the pipeline.
     *
     * Valid transitions:
     * PENDING → CONFIRMED → DISPATCHED → DELIVERED
     *
     * @Transactional: status update + history write + notification happen
     *                 atomically.
     *                 If the notification write fails, the status update also rolls
     *                 back.
     *
     *                 Phase 2 hook: Replace notificationService call with:
     *                 kafkaProducer.publish(new
     *                 OrderStatusChangedEvent(order.getId(), newStatus));
     *                 The Kafka transaction commits AFTER the DB transaction —
     *                 notifications
     *                 become fully decoupled from the order update.
     *
     * @throws ResourceNotFoundException if order not found
     * @throws IllegalStateException     if order is already in a terminal state
     */
    @Transactional
    public OrderResponse advanceOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus = getNextStatus(currentStatus);

        // Record old status before updating
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(nextStatus);
        order.setStatusUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Append to audit trail
        writeStatusHistory(order, oldStatus, nextStatus);

        // Notify customer of the transition
        notificationService.createNotification(
                order.getUser(),
                buildStatusChangeMessage(order, nextStatus)
        );

        return toResponse(order, false);
    }

    // ═══════════════════════════════════════════════════════════
    // CANCEL ORDER — PATCH /api/orders/{id}/cancel [CUSTOMER]
    // ═══════════════════════════════════════════════════════════

    /**
     * Cancels a PENDING order and restores stock.
     *
     * @Transactional ensures atomicity:
     *                1. Verify order is PENDING (throws if not)
     *                2. Verify the order belongs to this customer
     *                3. Set status to CANCELLED
     *                4. Restore stock for each item
     *                5. Write history record
     *                6. Notify customer
     *
     *                If stock restoration fails for any item, the ENTIRE
     *                transaction
     *                rolls back — the order remains PENDING and no stock is
     *                changed.
     *
     * @throws OrderCancellationException if order is not in PENDING status
     * @throws ResourceNotFoundException  if order not found or belongs to another
     *                                    user
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, User currentUser) {

        // Load order with items in one query (need items to restore stock)
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // ── Ownership check ─────────────────────────────────────────────
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Order", "id", orderId);
        }

        // ── Status guard: can only cancel PENDING orders ─────────────────
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderCancellationException(
                    String.format(
                            "Order can only be cancelled when in PENDING status. Current status: %s",
                            order.getStatus()));
        }

        OrderStatus oldStatus = order.getStatus();

        // ── Update status to CANCELLED ───────────────────────────────────
        order.setStatus(OrderStatus.CANCELLED);
        order.setStatusUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // ── Restore stock for each item in the cancelled order ───────────
        // This runs in the SAME transaction — if any restore fails, nothing is saved
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockCount(product.getStockCount() + item.getQuantity());
            productRepository.save(product);
        }

        // ── Write audit record ───────────────────────────────────────────
        writeStatusHistory(order, oldStatus, OrderStatus.CANCELLED);

        // ── Notify customer ──────────────────────────────────────────────
        notificationService.createNotification(
                currentUser,
                String.format("Your order #%d has been cancelled. Stock has been restored.", orderId));

        return toResponse(order, false);
    }

    // ═══════════════════════════════════════════════════════════
    // Private helpers
    // ═══════════════════════════════════════════════════════════

    /**
     * Determines the next valid status in the main pipeline.
     * CANCELLED and DELIVERED are terminal states — no further transitions.
     *
     * @throws IllegalStateException if the order is already in a terminal state
     */
    private OrderStatus getNextStatus(OrderStatus current) {
        return switch (current) {
            case PENDING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.DISPATCHED;
            case DISPATCHED -> OrderStatus.DELIVERED;
            case DELIVERED -> throw new IllegalStateException(
                    "Order is already delivered. No further status transitions are possible.");
            case CANCELLED -> throw new IllegalStateException(
                    "Order is cancelled. No further status transitions are possible.");
        };
    }

    /**
     * Builds a human-readable notification message for a status transition.
     * Extracted into a method so message format is consistent everywhere.
     */
    private String buildStatusChangeMessage(Order order, OrderStatus newStatus) {
        String productNames = order.getItems().stream()
                .map(item -> item.getProduct().getName())
                .collect(Collectors.joining(", "));
        
        String detail = switch (newStatus) {
            case CONFIRMED -> "has been confirmed and is being prepared.";
            case DISPATCHED -> "has been dispatched and is on its way!";
            case DELIVERED -> "has been delivered. Thank you!";
            default -> "status has been updated to " + newStatus;
        };
        return String.format("Update: Your order for [%s] %s", productNames, detail);
    }

    /**
     * Writes one record to order_status_history.
     * Called on EVERY status change — forms an immutable audit trail.
     * Running inside the caller's @Transactional context — no separate transaction.
     */
    private void writeStatusHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        historyRepository.save(history);
    }

    /**
     * Maps an Order entity to an OrderResponse DTO.
     *
     * @param order        the Order entity (may or may not have items loaded)
     * @param includeItems true for detail endpoint, false for list endpoint
     */
    private OrderResponse toResponse(Order order, boolean includeItems) {
        if (order == null)
            return null;

        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId());

        if (order.getUser() != null) {
            builder.userId(order.getUser().getId())
                    .customerName(order.getUser().getName());
        }

        builder.status(order.getStatus() != null ? order.getStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .statusUpdatedAt(order.getStatusUpdatedAt());

        if (includeItems && order.getItems() != null) {
            List<OrderItemResponse> itemResponses = order.getItems().stream()
                    .map((OrderItem item) -> toItemResponse(item))
                    .collect(Collectors.toList());
            builder.items(itemResponses);
        }

        return builder.build();
    }

    /** Maps an OrderItem entity to an OrderItemResponse DTO */
    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                // Line total = price × quantity
                .lineTotal(item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}