package com.project.order_processing_app.controller;

import com.project.order_processing_app.dto.request.ProductRequest;
import com.project.order_processing_app.dto.response.ProductResponse;
import com.project.order_processing_app.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ProductController — product catalogue management.
 *
 * Base path: /api/products
 *
 * Access rules:
 *   GET  /products     → PUBLIC  (anyone can browse, no token needed)
 *   GET  /products/{id}→ PUBLIC
 *   POST /products     → ADMIN only
 *   PUT  /products/{id}→ ADMIN only
 *   DELETE /products/{id} → ADMIN only
 *
 * @PreAuthorize("hasRole('ADMIN')") enforces role-based access at method level.
 * Requires @EnableMethodSecurity on SecurityConfig (already set).
 * If a CUSTOMER hits an admin endpoint → AccessDeniedException → 403 Forbidden.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─────────────────────────────────────────────────────────
    //  GET /api/products  [PUBLIC]
    // ─────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of products with optional filters.
     *
     * Query parameters (all optional):
     *   category  → exact match filter (e.g., ?category=Electronics)
     *   name      → partial, case-insensitive name search (e.g., ?name=laptop)
     *   page      → zero-based page number (default: 0)
     *   size      → items per page (default: 20)
     *
     * Postman test:
     *   GET http://localhost:8080/api/products
     *   GET http://localhost:8080/api/products?category=Electronics&page=0&size=10
     *   GET http://localhost:8080/api/products?name=laptop
     *
     * Expected: 200 with paginated list of products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(productService.getProducts(category, name, page, size));
    }

    // ─────────────────────────────────────────────────────────
    //  GET /api/products/{id}  [PUBLIC]
    // ─────────────────────────────────────────────────────────

    /**
     * Returns a single product by its ID.
     *
     * Postman test:
     *   GET http://localhost:8080/api/products/1
     *
     * Expected: 200 with product details
     * Error:    404 if product not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ─────────────────────────────────────────────────────────
    //  POST /api/products  [ADMIN only]
    // ─────────────────────────────────────────────────────────

    /**
     * Creates a new product. Admin only.
     *
     * Postman test:
     *   POST http://localhost:8080/api/products
     *   Authorization: Bearer <admin-token>
     *   Body (JSON):
     *   {
     *     "name": "Gaming Laptop",
     *     "description": "High-performance laptop with RTX 4060",
     *     "price": 89999.99,
     *     "stockCount": 50,
     *     "category": "Electronics"
     *   }
     *
     * Expected: 201 with created product (including generated ID)
     * Error:    403 if called with a CUSTOMER token
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────
    //  PUT /api/products/{id}  [ADMIN only]
    // ─────────────────────────────────────────────────────────

    /**
     * Updates all fields of an existing product. Admin only.
     *
     * Postman test:
     *   PUT http://localhost:8080/api/products/1
     *   Authorization: Bearer <admin-token>
     *   Body (JSON):
     *   {
     *     "name": "Gaming Laptop Pro",
     *     "description": "Updated description",
     *     "price": 94999.99,
     *     "stockCount": 30,
     *     "category": "Electronics"
     *   }
     *
     * Expected: 200 with updated product
     * Error:    404 if product not found, 403 if CUSTOMER token
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // ─────────────────────────────────────────────────────────
    //  DELETE /api/products/{id}  [ADMIN only]
    // ─────────────────────────────────────────────────────────

    /**
     * Deletes a product from the catalogue. Admin only.
     *
     * Postman test:
     *   DELETE http://localhost:8080/api/products/1
     *   Authorization: Bearer <admin-token>
     *
     * Expected: 204 No Content (success, nothing to return)
     * Error:    404 if not found, 403 if CUSTOMER token
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        // 204 No Content — operation succeeded, no body to return
        return ResponseEntity.noContent().build();
    }
}
