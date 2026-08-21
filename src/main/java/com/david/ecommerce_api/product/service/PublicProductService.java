package com.david.ecommerce_api.product.service;

import com.david.ecommerce_api.common.dto.PageResponse;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductStatus;
import com.david.ecommerce_api.product.repository.ProductRepository;
import com.david.ecommerce_api.product.repository.ProductReviewRepository;
import com.david.ecommerce_api.product.repository.ProductSpecifications;
import com.david.ecommerce_api.product.web.ProductResponses;
import com.david.ecommerce_api.product.web.ProductWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicProductService {

    private static final int NEW_ARRIVAL_DAYS = 30;

    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductWebMapper productWebMapper;

    public PageResponse<ProductResponses.Card> getProducts(
            String q,
            Boolean onSale,
            String sort,
            int page,
            int size
    ) {
        Pageable pageable = buildProductPageable(page, size, sort);

        Page<Product> products;

        if (hasText(q) || onSale != null) {
            Specification<Product> specification = buildProductSpecification(q, onSale);

            products = productRepository.findAll(specification, pageable);
        } else {
            products = productRepository.findByStatus(
                    ProductStatus.PUBLISHED,
                    pageable
            );
        }

        return PageResponse.from(
                products.map(productWebMapper::toCard)
        );
    }

    public PageResponse<ProductResponses.Card> getNewArrivals(
            int page,
            int size
    ) {
        Instant fromDate = Instant.now()
                .minusSeconds(NEW_ARRIVAL_DAYS * 24L * 60L * 60L);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("publishedAt"),
                        Sort.Order.desc("id")
                )
        );

        return PageResponse.from(
                productRepository
                        .findByStatusAndPublishedAtGreaterThanEqual(
                                ProductStatus.PUBLISHED,
                                fromDate,
                                pageable
                        )
                        .map(productWebMapper::toCard)
        );
    }

    public PageResponse<ProductResponses.Card> getTopSelling(
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("totalSold"),
                        Sort.Order.desc("ratingAverage"),
                        Sort.Order.desc("id")
                )
        );

        return PageResponse.from(
                productRepository
                        .findByStatusAndTotalSoldGreaterThan(
                                ProductStatus.PUBLISHED,
                                0,
                                pageable
                        )
                        .map(productWebMapper::toCard)
        );
    }

    public PageResponse<ProductResponses.Card> getOnSale(
            String sort,
            int page,
            int size
    ) {
        return getProducts(
                null,
                true,
                sort,
                page,
                size
        );
    }

    public ProductResponses.Detail getProductDetail(String slug) {
        Product product = productRepository
                .findBySlugAndStatus(
                        slug,
                        ProductStatus.PUBLISHED
                )
                .orElseThrow(() -> new DomainException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "product"
                ));

        return productWebMapper.toDetail(product);
    }

    public PageResponse<ProductResponses.Review> getProductReviews(
            String slug,
            String sort,
            int page,
            int size
    ) {
        Pageable pageable = buildReviewPageable(page, size, sort);

        return PageResponse.from(
                productReviewRepository
                        .findByProductSlugAndProductStatusAndApprovedTrue(
                                slug,
                                ProductStatus.PUBLISHED,
                                pageable
                        )
                        .map(productWebMapper::toReview)
        );
    }

    private Specification<Product> buildProductSpecification(
            String q,
            Boolean onSale
    ) {
        Specification<Product> specification =
                ProductSpecifications.hasStatus(ProductStatus.PUBLISHED);

        if (hasText(q)) {
            specification = specification.and(
                    ProductSpecifications.containsText(q.trim())
            );
        }

        if (onSale != null) {
            specification = specification.and(
                    ProductSpecifications.onSale(onSale)
            );
        }

        return specification;
    }

    private Pageable buildProductPageable(
            int page,
            int size,
            String sort
    ) {
        Sort springSort = switch (normalizeSort(sort)) {
            case "price-asc" -> Sort.by(
                    Sort.Order.asc("price"),
                    Sort.Order.desc("id")
            );

            case "price-desc" -> Sort.by(
                    Sort.Order.desc("price"),
                    Sort.Order.desc("id")
            );

            case "rating" -> Sort.by(
                    Sort.Order.desc("ratingAverage"),
                    Sort.Order.desc("ratingCount"),
                    Sort.Order.desc("id")
            );

            case "top-selling" -> Sort.by(
                    Sort.Order.desc("totalSold"),
                    Sort.Order.desc("id")
            );

            default -> Sort.by(
                    Sort.Order.desc("publishedAt"),
                    Sort.Order.desc("id")
            );
        };

        return PageRequest.of(page, size, springSort);
    }

    private Pageable buildReviewPageable(
            int page,
            int size,
            String sort
    ) {
        Sort springSort = switch (normalizeSort(sort)) {
            case "oldest" -> Sort.by(
                    Sort.Order.asc("createdAt"),
                    Sort.Order.asc("id")
            );

            default -> Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );
        };

        return PageRequest.of(page, size, springSort);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "latest";
        }

        return sort.trim().toLowerCase();
    }
}