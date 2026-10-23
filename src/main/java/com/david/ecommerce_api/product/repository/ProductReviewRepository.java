package com.david.ecommerce_api.product.repository;

import com.david.ecommerce_api.product.domain.ProductReview;
import com.david.ecommerce_api.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Page<ProductReview> findByProductSlugAndProductStatusAndApprovedTrue(
            String slug,
            ProductStatus status,
            Pageable pageable
    );

    List<ProductReview> findByProductIdAndApprovedTrueOrderByIdDesc(Long productId);

    List<ProductReview> findByProductIdAndApprovedTrue(Long productId);

    List<ProductReview> findAllByOrderByIdDesc();

    List<ProductReview> findByProductIdOrderByIdDesc(Long productId);
}
