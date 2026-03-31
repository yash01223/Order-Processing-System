package com.project.order_processing_app.repository;

import com.project.order_processing_app.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("SELECT p FROM Product p WHERE " +
            "(:category = '' OR p.category = :category) AND " +
            "(:name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Product> findAllWithFilters(
            @Param("category") String category,
            @Param("name") String name,
            Pageable pageable
    );
}