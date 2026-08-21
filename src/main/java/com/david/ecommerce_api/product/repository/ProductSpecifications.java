package com.david.ecommerce_api.product.repository;

import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> hasStatus(ProductStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Product> containsText(String text) {
        return (root, query, criteriaBuilder) -> {
            String value = "%" + text.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            value
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("shortDescription")),
                            value
                    )
            );
        };
    }

    public static Specification<Product> onSale(boolean onSale) {
        return (root, query, criteriaBuilder) -> {
            var salePredicate = criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("compareAtPrice")),
                    criteriaBuilder.greaterThan(
                            root.get("compareAtPrice"),
                            root.get("price")
                    )
            );

            return onSale
                    ? salePredicate
                    : criteriaBuilder.not(salePredicate);
        };
    }
}