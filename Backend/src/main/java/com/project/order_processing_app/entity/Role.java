package com.project.order_processing_app.entity;

/**
 * Role — defines the two user roles in the system.
 *
 * CUSTOMER : Can browse products, place orders, view their own orders,
 *            cancel orders (PENDING only), and read notifications.
 *
 * ADMIN    : Can create/update/delete products, advance order statuses
 *            through the pipeline, and view dashboard statistics.
 *
 * Stored as STRING in the DB via @Enumerated(EnumType.STRING) on the User entity.
 * Using STRING (not ORDINAL) means adding new roles in the future
 * won't corrupt existing data rows.
 *
 * Spring Security expects the prefix "ROLE_" when using @PreAuthorize.
 * e.g., @PreAuthorize("hasRole('ADMIN')") matches authority "ROLE_ADMIN"
 * → See User.getAuthorities() where "ROLE_" prefix is added.
 */
public enum Role {
    CUSTOMER,
    ADMIN
}