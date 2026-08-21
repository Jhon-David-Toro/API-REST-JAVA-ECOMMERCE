package com.david.ecommerce_api.product.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ProductResponses {

    private ProductResponses() {
        /*
         * Utility class.
         * It should not be instantiated.
         */
    }

    public record Card(
            Long id,
            String slug,
            String name,
            String thumbnailUrl,
            String thumbnailAlt,
            BigDecimal price,
            BigDecimal compareAtPrice,
            Integer discountPercent,
            double ratingAverage,
            int ratingCount,
            boolean onSale,
            boolean newArrival,
            boolean topSelling
    ) {}

    public record Detail(
            Long id,
            String slug,
            String name,
            String shortDescription,

            BigDecimal price,
            BigDecimal compareAtPrice,
            Integer discountPercent,

            double ratingAverage,
            int ratingCount,

            boolean onSale,
            boolean newArrival,
            boolean topSelling,
            boolean available,
            int availableStock,

            List<Image> gallery,
            List<Color> colors,
            List<Size> sizes,
            List<Variant> variants,

            Description description,
            List<Feature> features,
            List<Faq> faqs
    ) {}

    public record Image(
            Long id,
            String imageUrl,
            String alt,
            int displayOrder,
            boolean mainImage
    ) {}

    public record Color(
            Long id,
            String name,
            String hex,
            boolean active,
            int availableStock
    ) {}

    public record Size(
            Long id,
            String name,
            String value,
            boolean active,
            int availableStock
    ) {}

    public record Variant(
            Long id,
            String sku,

            Long colorId,
            String colorName,

            Long sizeId,
            String sizeName,
            String sizeValue,

            BigDecimal effectivePrice,
            int stock,
            boolean active,
            boolean available
    ) {}

    public record Description(
            String overview,
            String origin,
            String companyName,
            String aboutCompany,
            String careInstructions
    ) {}

    public record Feature(
            Long id,
            String text,
            int displayOrder
    ) {}

    public record Faq(
            Long id,
            String question,
            String answer,
            int displayOrder
    ) {}

    public record Review(
            Long id,
            String customerName,
            int rating,
            String comment,
            boolean verified,
            Instant createdAt
    ) {}
}