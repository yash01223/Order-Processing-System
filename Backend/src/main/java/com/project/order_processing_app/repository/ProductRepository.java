package com.project.order_processing_app.repository;

import com.project.order_processing_app.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ProductRepository — Spring Data JPA repository for the Product entity.
 *
 * The custom JPQL query supports simultaneous optional category + name filtering
 * with pagination. Both filters are nullable — passing null skips that filter entirely.
 *
 * Phase 2 hook:
 *   ProductService.getProducts() and getProductById() will receive @Cacheable("products").
 *   This repository remains completely unchanged — caching is a service-layer concern.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Paginated product listing with optional category and name search filters.
     *
     * JPQL explanation:
     *   (:category IS NULL OR p.category = :category)
     *     → If null is passed for category, this condition is always TRUE (no filter applied).
     *     → If a value is passed, it must match exactly.
     *
     *   (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
     *     → If null is passed for name, skip the filter.
     *     → If a value is passed, does a case-insensitive contains search.
     *     → CONCAT('%', :name, '%') wraps the search term with wildcards.
     *
     * Pageable handles: page number, page size, and sort field/direction.
     *
     * Example usage:
     *   findAllWithFilters("Electronics", "phone", PageRequest.of(0, 20, Sort.by("name")))
     *
     * Generated SQL (approximate):
     *   SELECT * FROM products
     *   WHERE (category = 'Electronics')
     *     AND (LOWER(name) LIKE '%phone%')
     *   ORDER BY name ASC
     *   LIMIT 20 OFFSET 0
     *
     * @param category  exact category match, or null to skip
     * @param name      partial name search (case-insensitive), or null to skip
     * @param pageable  pagination + sort parameters
     * @return          one page of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
            "p.deleted = false AND " +
            "(:category = '' OR p.category = :category) AND " +
            "(:name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Product> findAllWithFilters(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable
    );
}