package com.david.ecommerce_api.product.repository;

import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends
        JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlugAndStatus(
            String slug,
            ProductStatus status
    );

    Page<Product> findByStatus(
            ProductStatus status,
            Pageable pageable
    );

    Page<Product> findByStatusAndPublishedAtGreaterThanEqual(
            ProductStatus status,
            Instant publishedAt,
            Pageable pageable
    );

    Page<Product> findByStatusAndTotalSoldGreaterThan(
            ProductStatus status,
            long totalSold,
            Pageable pageable
    );

    List<Product> findByStatusOrderByIdDesc(ProductStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
