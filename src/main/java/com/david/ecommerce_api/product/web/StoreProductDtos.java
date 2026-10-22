package com.david.ecommerce_api.product.web;

import java.math.BigDecimal;
import java.util.List;

/**
 * Complete view of a published product for the storefront: everything the catalog pages, the filters and
 * the product page need in one document.
 */
public final class StoreProductDtos {

    private StoreProductDtos() {
    }

    public record Color(String name, String hex) {
    }

    public record Company(String name, String description) {
    }

    public record Description(
            String overview,
            List<String> features,
            String origin,
            Company company,
            String careInstructions
    ) {
    }

    public record Faq(String id, String question, String answer) {
    }

    public record StoreProduct(
            Long id,
            String name,
            String slug,
            String brand,
            String category,
            String dressStyle,
            BigDecimal price,
            BigDecimal discountPrice,
            int discount,
            double rating,
            int reviewsCount,
            String image,
            List<String> images,
            List<Color> colors,
            List<String> sizes,
            int stock,
            String status,
            long createdAt,
            Description description,
            List<Faq> faqs
    ) {
    }
}
