package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.product.domain.ProductStatus;
import com.david.ecommerce_api.product.repository.ProductRepository;
import com.david.ecommerce_api.product.web.StoreProductDtos.StoreProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Every published product in one document, so the storefront can filter, sort and paginate on its own.
 * Meant for catalogs of a few hundred products.
 */
@RestController
@RequestMapping("/api/public/catalog")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final ProductRepository productRepository;
    private final StoreProductMapper mapper;

    @GetMapping
    @Transactional(readOnly = true)
    public List<StoreProduct> list() {
        return productRepository.findByStatusOrderByIdDesc(ProductStatus.PUBLISHED).stream()
                .map(mapper::toStoreProduct)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public StoreProduct get(@PathVariable Long id) {
        return productRepository.findById(id)
                .filter(product -> product.isPublished())
                .map(mapper::toStoreProduct)
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
