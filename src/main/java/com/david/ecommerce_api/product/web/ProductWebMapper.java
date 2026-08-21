package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.product.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
public class ProductWebMapper {

    private static final int NEW_ARRIVAL_DAYS = 30;
    private static final long TOP_SELLING_MIN_SALES = 50;

    public ProductResponses.Card toCard(Product product) {
        return new ProductResponses.Card(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getThumbnailUrl(),
                product.getThumbnailAlt(),
                product.getPrice(),
                product.getCompareAtPrice(),
                calculateDiscountPercent(
                        product.getPrice(),
                        product.getCompareAtPrice()
                ),
                product.getRatingAverage(),
                product.getRatingCount(),
                isOnSale(product.getPrice(), product.getCompareAtPrice()),
                isNewArrival(product.getPublishedAt()),
                isTopSelling(product.getTotalSold())
        );
    }

    public ProductResponses.Detail toDetail(Product product) {
        return new ProductResponses.Detail(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getShortDescription(),

                product.getPrice(),
                product.getCompareAtPrice(),
                calculateDiscountPercent(
                        product.getPrice(),
                        product.getCompareAtPrice()
                ),

                product.getRatingAverage(),
                product.getRatingCount(),

                isOnSale(product.getPrice(), product.getCompareAtPrice()),
                isNewArrival(product.getPublishedAt()),
                isTopSelling(product.getTotalSold()),
                product.isAvailable(),
                product.getAvailableStock(),

                mapImages(product),
                mapColors(product),
                mapSizes(product),
                mapVariants(product),

                mapDescription(product),
                mapFeatures(product),
                mapFaqs(product)
        );
    }

    public ProductResponses.Review toReview(ProductReview review) {
        return new ProductResponses.Review(
                review.getId(),
                review.getCustomerName(),
                review.getRating(),
                review.getComment(),
                review.isVerified(),
                review.getCreatedAt()
        );
    }

    private List<ProductResponses.Image> mapImages(Product product) {
        return product.getImages()
                .stream()
                .sorted(Comparator.comparing(ProductImage::getDisplayOrder))
                .map(image -> new ProductResponses.Image(
                        image.getId(),
                        image.getImageUrl(),
                        image.getAlt(),
                        image.getDisplayOrder(),
                        image.isMainImage()
                ))
                .toList();
    }

    private List<ProductResponses.Color> mapColors(Product product) {
        return product.getColors()
                .stream()
                .filter(ProductColor::isActive)
                .sorted(Comparator.comparing(ProductColor::getDisplayOrder))
                .map(color -> new ProductResponses.Color(
                        color.getId(),
                        color.getName(),
                        color.getHex(),
                        color.isActive(),
                        product.getAvailableStockForColor(color)
                ))
                .toList();
    }

    private List<ProductResponses.Size> mapSizes(Product product) {
        return product.getSizes()
                .stream()
                .filter(ProductSize::isActive)
                .sorted(Comparator.comparing(ProductSize::getDisplayOrder))
                .map(size -> new ProductResponses.Size(
                        size.getId(),
                        size.getName(),
                        size.getValue(),
                        size.isActive(),
                        product.getAvailableStockForSize(size)
                ))
                .toList();
    }

    private List<ProductResponses.Variant> mapVariants(Product product) {
        return product.getVariants()
                .stream()
                .filter(ProductVariant::isActive)
                .map(variant -> new ProductResponses.Variant(
                        variant.getId(),
                        variant.getSku(),

                        variant.getColor().getId(),
                        variant.getColor().getName(),

                        variant.getSize().getId(),
                        variant.getSize().getName(),
                        variant.getSize().getValue(),

                        variant.getEffectivePrice(),
                        variant.getStock(),
                        variant.isActive(),
                        variant.isAvailable()
                ))
                .toList();
    }

    private ProductResponses.Description mapDescription(Product product) {
        ProductDetail detail = product.getDetail();

        if (detail == null) {
            return null;
        }

        return new ProductResponses.Description(
                detail.getOverview(),
                detail.getOrigin(),
                detail.getCompanyName(),
                detail.getAboutCompany(),
                detail.getCareInstructions()
        );
    }

    private List<ProductResponses.Feature> mapFeatures(Product product) {
        return product.getFeatures()
                .stream()
                .sorted(Comparator.comparing(ProductFeature::getDisplayOrder))
                .map(feature -> new ProductResponses.Feature(
                        feature.getId(),
                        feature.getText(),
                        feature.getDisplayOrder()
                ))
                .toList();
    }

    private List<ProductResponses.Faq> mapFaqs(Product product) {
        return product.getFaqs()
                .stream()
                .filter(ProductFaq::isActive)
                .sorted(Comparator.comparing(ProductFaq::getDisplayOrder))
                .map(faq -> new ProductResponses.Faq(
                        faq.getId(),
                        faq.getQuestion(),
                        faq.getAnswer(),
                        faq.getDisplayOrder()
                ))
                .toList();
    }

    private boolean isOnSale(
            BigDecimal price,
            BigDecimal compareAtPrice
    ) {
        return compareAtPrice != null
                && price != null
                && compareAtPrice.compareTo(price) > 0;
    }

    private Integer calculateDiscountPercent(
            BigDecimal price,
            BigDecimal compareAtPrice
    ) {
        if (!isOnSale(price, compareAtPrice)) {
            return null;
        }

        BigDecimal discount = compareAtPrice.subtract(price);

        return discount
                .multiply(BigDecimal.valueOf(100))
                .divide(compareAtPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private boolean isNewArrival(Instant publishedAt) {
        if (publishedAt == null) {
            return false;
        }

        Instant limit = Instant.now()
                .minusSeconds(NEW_ARRIVAL_DAYS * 24L * 60L * 60L);

        return publishedAt.isAfter(limit);
    }

    private boolean isTopSelling(long totalSold) {
        return totalSold >= TOP_SELLING_MIN_SALES;
    }
}