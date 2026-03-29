package com.project.order_processing_app.repository;

import com.project.order_processing_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — Spring Data JPA repository for the User entity.
 *
 * By extending JpaRepository<User, Long>, Spring auto-generates
 * full CRUD + paging operations at runtime — no implementation needed:
 *   save(user)           → INSERT or UPDATE
 *   findById(id)         → SELECT WHERE id = ?
 *   findAll()            → SELECT all
 *   delete(user)         → DELETE WHERE id = ?
 *   count()              → SELECT COUNT(*)
 *   existsById(id)       → SELECT COUNT(*) > 0 WHERE id = ?
 *
 * Custom query methods follow Spring Data's derived query naming:
 *   findBy<Field>        → SELECT * FROM users WHERE field = ?
 *   existsBy<Field>      → SELECT COUNT(*) > 0 FROM users WHERE field = ?
 * Spring parses the method name and generates the JPQL at startup.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * Used in:
     *   - AuthService.login()     → verify credentials during login
     *   - JwtAuthFilter           → load user from token's email claim on each request
     *
     * Returns Optional<User> to force the caller to handle "user not found" explicitly,
     * avoiding NullPointerException.
     *
     * Generated SQL: SELECT * FROM users WHERE email = ? LIMIT 1
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if an email is already registered.
     *
     * Used in AuthService.register() to prevent duplicate accounts.
     * More efficient than findByEmail() + isPresent() — only runs COUNT query.
     *
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}