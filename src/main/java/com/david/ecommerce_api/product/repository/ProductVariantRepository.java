package com.david.ecommerce_api.product.repository;

import com.david.ecommerce_api.product.domain.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductVariant> findByIdAndActiveTrue(Long id);

    Optional<ProductVariant> findByProductIdAndColorIdAndSizeIdAndActiveTrue(
            Long productId,
            Long colorId,
            Long sizeId
    );
}