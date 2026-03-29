package com.project.order_processing_app.service;

import com.project.order_processing_app.dto.request.ProductRequest;
import com.project.order_processing_app.dto.response.ProductResponse;
import com.project.order_processing_app.exception.ResourceNotFoundException;
import com.project.order_processing_app.entity.product.Product;
import com.project.order_processing_app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductService — business logic for all product operations.
 *
 * Phase 2 hooks (marked with comments):
 * getProductById() and getProducts() → @Cacheable("products")
 * updateProduct() and deleteProduct() → @CacheEvict("products")
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // ═══════════════════════════════════════════════════════════
    // CREATE — POST /api/products [ADMIN only]
    // ═══════════════════════════════════════════════════════════

    /**
     * Creates a new product in the catalogue.
     *
     * @Transactional: ensures the INSERT is rolled back if anything fails.
     *
     * @param request validated ProductRequest from AdminController
     * @return ProductResponse with the newly generated ID
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockCount(request.getStockCount())
                .category(request.getCategory())
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // READ ALL — GET /api/products [Public]
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns a paginated list of products with optional filters.
     *
     * Phase 2 hook: add @Cacheable("products") here — zero logic change.
     *
     * @param category optional exact category filter (null = all categories)
     * @param name     optional partial name search, case-insensitive (null = all
     *                 names)
     * @param page     zero-based page number (default 0)
     * @param size     number of items per page (default 20)
     * @return a Page of ProductResponse objects
     */
    // Phase 2: @Cacheable("products")
    public Page<ProductResponse> getProducts(String category, String name, int page, int size) {
        // Sort by name ascending by default — deterministic ordering for pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        String catFilter = (category == null) ? "" : category;
        String nameFilter = (name == null) ? "" : name;
        return productRepository
                .findAllWithFilters(catFilter, nameFilter, pageable)
                .map(this::toResponse); // Stream each Product entity through toResponse()
    }

    // ═══════════════════════════════════════════════════════════
    // READ ONE — GET /api/products/{id} [Public]
    // ═══════════════════════════════════════════════════════════

    /**
     * Returns a single product by ID.
     * Phase 2 hook: add @Cacheable("products") here.
     *
     * @throws ResourceNotFoundException if no product with the given ID exists
     */
    // Phase 2: @Cacheable(value = "products", key = "#id")

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toResponse(product);
    }

    // ═══════════════════════════════════════════════════════════
    // UPDATE — PUT /api/products/{id} [ADMIN only]
    // ═══════════════════════════════════════════════════════════

    /**
     * Updates all fields of an existing product.
     *
     * Phase 2 hook: add @CacheEvict(value="products", allEntries=true) here
     * to invalidate the product cache after an update.
     *
     * @Transactional: the SELECT + UPDATE happen atomically.
     *
     * @throws ResourceNotFoundException if the product doesn't exist
     */
    // Phase 2: @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        // Load existing entity — throws 404 if not found
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Update all fields from the request
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockCount(request.getStockCount());
        product.setCategory(request.getCategory());

        // save() on an existing entity → UPDATE SQL (Hibernate detects dirty fields)
        Product updated = productRepository.save(product);
        return toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════════
    // DELETE — DELETE /api/products/{id} [ADMIN only]
    // ═══════════════════════════════════════════════════════════

    /**
     * Deletes a product from the catalogue.
     * Phase 2 hook: add @CacheEvict(value="products", allEntries=true) here.
     *
     * @throws ResourceNotFoundException if the product doesn't exist
     */
    // Phase 2: @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        // Soft delete: keep the record for order history, but hide it everywhere else
        product.setDeleted(true);
        productRepository.save(product);
    }

    // ═══════════════════════════════════════════════════════════
    // Private mapper — Product entity → ProductResponse DTO
    // ═══════════════════════════════════════════════════════════

    /**
     * Maps a Product entity to a ProductResponse DTO.
     * Computes the inStock convenience field from stockCount.
     *
     * WHY map in the service layer (not the controller)?
     * - Service methods return DTOs → controllers stay thin
     * - If the entity structure changes, only this mapper needs to update
     * - The controller never has direct access to entity internals
     */

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockCount(product.getStockCount())
                .category(product.getCategory())
                .inStock(product.getStockCount() > 0) // Computed field
                .build();
    }
}