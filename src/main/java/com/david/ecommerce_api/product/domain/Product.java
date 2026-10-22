package com.david.ecommerce_api.product.domain;

import com.david.ecommerce_api.brand.Brand;
import com.david.ecommerce_api.category.Category;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_slug", columnList = "slug"),
                @Index(name = "idx_products_status", columnList = "status"),
                @Index(name = "idx_products_published_at", columnList = "published_at"),
                @Index(name = "idx_products_total_sold", columnList = "total_sold")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(name = "short_description", length = 1000)
    private String shortDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "total_sold", nullable = false)
    private long totalSold = 0L;

    @Column(name = "rating_average", nullable = false)
    private double ratingAverage = 0.0;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount = 0;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "thumbnail_alt", length = 250)
    private String thumbnailAlt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "dress_style", length = 60)
    private String dressStyle;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ProductColor> colors = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ProductSize> sizes = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToOne(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private ProductDetail detail;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ProductFeature> features = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ProductFaq> faqs = new ArrayList<>();

    private Product(
            String name,
            String slug,
            String shortDescription,
            BigDecimal price,
            BigDecimal compareAtPrice
    ) {
        rename(name);
        changeSlug(slug);
        updateShortDescription(shortDescription);
        changePricing(price, compareAtPrice);
    }

    public static Product createDraft(
            String name,
            String slug,
            String shortDescription,
            BigDecimal price,
            BigDecimal compareAtPrice
    ) {
        return new Product(name, slug, shortDescription, price, compareAtPrice);
    }

    public void rename(String name) {
        Guard.notBlank(name, ErrorCode.VALIDATION_ERROR, "name");
        this.name = name.trim();
    }

    public void changeSlug(String slug) {
        Guard.notBlank(slug, ErrorCode.VALIDATION_ERROR, "slug");
        this.slug = slug.trim().toLowerCase();
    }

    public void updateShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void changePricing(BigDecimal price, BigDecimal compareAtPrice) {
        Guard.positive(price, ErrorCode.PRODUCT_INVALID_PRICE);

        if (compareAtPrice != null) {
            Guard.isTrue(
                    compareAtPrice.compareTo(price) > 0,
                    ErrorCode.PRODUCT_INVALID_COMPARE_AT_PRICE
            );
        }

        this.price = price;
        this.compareAtPrice = compareAtPrice;
    }

    public void publish() {
        transitionTo(ProductStatus.PUBLISHED);
        ensurePublishable();

        this.status = ProductStatus.PUBLISHED;
        this.publishedAt = this.publishedAt == null ? Instant.now() : this.publishedAt;
    }

    public void unpublish() {
        transitionTo(ProductStatus.DRAFT);
        this.status = ProductStatus.DRAFT;
    }

    public void archive() {
        transitionTo(ProductStatus.ARCHIVED);
        this.status = ProductStatus.ARCHIVED;
    }

    public void restoreToDraft() {
        transitionTo(ProductStatus.DRAFT);
        this.status = ProductStatus.DRAFT;
    }

    private void transitionTo(ProductStatus targetStatus) {
        if (this.status == targetStatus) {
            return;
        }

        boolean allowed = switch (this.status) {
            case DRAFT -> targetStatus == ProductStatus.PUBLISHED
                    || targetStatus == ProductStatus.ARCHIVED;

            case PUBLISHED -> targetStatus == ProductStatus.DRAFT
                    || targetStatus == ProductStatus.ARCHIVED;

            case ARCHIVED -> targetStatus == ProductStatus.DRAFT;
        };

        Guard.isTrue(
                allowed,
                ErrorCode.PRODUCT_INVALID_STATUS_TRANSITION,
                this.status,
                targetStatus
        );
    }

    private void ensurePublishable() {
        boolean hasImage = !this.images.isEmpty();
        boolean hasActiveVariant = this.variants.stream().anyMatch(ProductVariant::isActive);

        Guard.isTrue(hasImage, ErrorCode.PRODUCT_NOT_PUBLISHABLE, "image_required");
        Guard.isTrue(hasActiveVariant, ErrorCode.PRODUCT_NOT_PUBLISHABLE, "variant_required");
    }

    public boolean isPublished() {
        return this.status == ProductStatus.PUBLISHED;
    }

    public boolean isOnSale() {
        return this.compareAtPrice != null
                && this.compareAtPrice.compareTo(this.price) > 0;
    }

    public int getAvailableStock() {
        return this.variants
                .stream()
                .filter(ProductVariant::isActive)
                .mapToInt(ProductVariant::getStock)
                .sum();
    }

    public boolean isAvailable() {
        return getAvailableStock() > 0;
    }

    public int getAvailableStockForColor(ProductColor color) {
        assertColorBelongsToProduct(color);

        return this.variants
                .stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> variant.hasColor(color))
                .mapToInt(ProductVariant::getStock)
                .sum();
    }

    public int getAvailableStockForSize(ProductSize size) {
        assertSizeBelongsToProduct(size);

        return this.variants
                .stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> variant.hasSize(size))
                .mapToInt(ProductVariant::getStock)
                .sum();
    }

    public ProductVariant getVariant(ProductColor color, ProductSize size) {
        assertColorBelongsToProduct(color);
        assertSizeBelongsToProduct(size);

        return this.variants
                .stream()
                .filter(variant -> variant.matches(color, size))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
    }

    public void increaseSold(long quantity) {
        Guard.isTrue(quantity > 0, ErrorCode.STOCK_INVALID_QUANTITY, quantity);
        this.totalSold += quantity;
    }

    public void decreaseSold(long quantity) {
        Guard.isTrue(quantity > 0, ErrorCode.STOCK_INVALID_QUANTITY, quantity);
        this.totalSold = Math.max(0L, this.totalSold - quantity);
    }

    public void updateRating(double ratingAverage, int ratingCount) {
        Guard.isTrue(ratingAverage >= 0 && ratingAverage <= 5, ErrorCode.VALIDATION_ERROR, "ratingAverage");
        Guard.isTrue(ratingCount >= 0, ErrorCode.VALIDATION_ERROR, "ratingCount");

        this.ratingAverage = ratingAverage;
        this.ratingCount = ratingCount;
    }

    public ProductImage addImage(
            String imageUrl,
            String publicId,
            String alt,
            int displayOrder,
            boolean mainImage,
            Integer width,
            Integer height
    ) {
        ProductImage image = ProductImage.create(
                imageUrl,
                publicId,
                alt,
                displayOrder,
                mainImage,
                width,
                height
        );

        image.attachTo(this);

        if (mainImage) {
            this.images.forEach(ProductImage::unmarkAsMain);
            this.thumbnailUrl = imageUrl;
            this.thumbnailAlt = alt;
        }

        if (this.thumbnailUrl == null) {
            this.thumbnailUrl = imageUrl;
            this.thumbnailAlt = alt;
        }

        this.images.add(image);

        return image;
    }

    public void markImageAsMain(Long imageId) {
        ProductImage image = this.images
                .stream()
                .filter(item -> item.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        this.images.forEach(ProductImage::unmarkAsMain);
        image.markAsMain();

        this.thumbnailUrl = image.getImageUrl();
        this.thumbnailAlt = image.getAlt();
    }

    public ProductImage removeImage(Long imageId) {
        ProductImage image = this.images
                .stream()
                .filter(item -> item.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));

        this.images.remove(image);
        image.detach();

        if (image.getImageUrl().equals(this.thumbnailUrl)) {
            ProductImage fallback = this.images.stream().findFirst().orElse(null);

            this.thumbnailUrl = fallback != null ? fallback.getImageUrl() : null;
            this.thumbnailAlt = fallback != null ? fallback.getAlt() : null;

            if (fallback != null) {
                fallback.markAsMain();
            }
        }

        return image;
    }

    public ProductColor addColor(String name, String hex, int displayOrder) {
        boolean duplicated = this.colors
                .stream()
                .anyMatch(color -> color.hasSameName(name));

        Guard.isFalse(duplicated, ErrorCode.VALIDATION_ERROR, "duplicated_color");

        ProductColor color = ProductColor.create(name, hex, displayOrder);
        color.attachTo(this);

        this.colors.add(color);

        return color;
    }

    public void deactivateColor(ProductColor color) {
        assertColorBelongsToProduct(color);

        color.deactivate();

        this.variants
                .stream()
                .filter(variant -> variant.hasColor(color))
                .forEach(ProductVariant::deactivate);
    }

    public ProductSize addSize(String name, String value, int displayOrder) {
        boolean duplicated = this.sizes
                .stream()
                .anyMatch(size -> size.hasSameValue(value));

        Guard.isFalse(duplicated, ErrorCode.VALIDATION_ERROR, "duplicated_size");

        ProductSize size = ProductSize.create(name, value, displayOrder);
        size.attachTo(this);

        this.sizes.add(size);

        return size;
    }

    public void deactivateSize(ProductSize size) {
        assertSizeBelongsToProduct(size);

        size.deactivate();

        this.variants
                .stream()
                .filter(variant -> variant.hasSize(size))
                .forEach(ProductVariant::deactivate);
    }

    public ProductVariant addVariant(
            ProductColor color,
            ProductSize size,
            String sku,
            int initialStock,
            BigDecimal priceOverride
    ) {
        assertColorBelongsToProduct(color);
        assertSizeBelongsToProduct(size);

        Guard.isTrue(color.isActive(), ErrorCode.PRODUCT_OPTION_DOES_NOT_BELONG, "inactive_color");
        Guard.isTrue(size.isActive(), ErrorCode.PRODUCT_OPTION_DOES_NOT_BELONG, "inactive_size");

        boolean duplicated = this.variants
                .stream()
                .anyMatch(variant -> variant.matches(color, size));

        Guard.isFalse(duplicated, ErrorCode.PRODUCT_VARIANT_DUPLICATED, color.getName(), size.getValue());

        ProductVariant variant = ProductVariant.create(
                this,
                color,
                size,
                sku,
                initialStock,
                priceOverride
        );

        this.variants.add(variant);

        return variant;
    }

    public void removeVariant(ProductVariant variant) {
        this.variants.remove(variant);
        variant.detach();
    }

    public void setDetail(ProductDetail detail) {
        this.detail = detail;

        if (detail != null) {
            detail.attachTo(this);
        }
    }

    public ProductFeature addFeature(String text, int displayOrder) {
        ProductFeature feature = ProductFeature.create(text, displayOrder);
        feature.attachTo(this);

        this.features.add(feature);

        return feature;
    }

    public ProductFaq addFaq(String question, String answer, int displayOrder) {
        ProductFaq faq = ProductFaq.create(question, answer, displayOrder);
        faq.attachTo(this);

        this.faqs.add(faq);

        return faq;
    }

    public void classify(Category category, Brand brand, String dressStyle) {
        this.category = category;
        this.brand = brand;
        this.dressStyle = dressStyle == null || dressStyle.isBlank() ? null : dressStyle.trim();
    }

    public void clearImages() {
        this.images.forEach(ProductImage::detach);
        this.images.clear();
        this.thumbnailUrl = null;
        this.thumbnailAlt = null;
    }

    public void clearFeatures() {
        this.features.clear();
    }

    public void clearFaqs() {
        this.faqs.clear();
    }

    public void updateDetail(
            String overview,
            String origin,
            String companyName,
            String aboutCompany,
            String careInstructions
    ) {
        if (this.detail == null) {
            setDetail(ProductDetail.create(overview, origin, companyName, aboutCompany, careInstructions));
            return;
        }

        this.detail.update(overview, origin, companyName, aboutCompany, careInstructions);
    }

    public Optional<ProductColor> findColor(String name) {
        return this.colors.stream().filter(color -> color.hasSameName(name)).findFirst();
    }

    public Optional<ProductSize> findSize(String value) {
        return this.sizes.stream().filter(size -> size.hasSameValue(value)).findFirst();
    }

    /**
     * Creates the color, or updates and reactivates the one that already has that name.
     */
    public ProductColor upsertColor(String name, String hex, int displayOrder) {
        Optional<ProductColor> existing = this.colors.stream()
                .filter(color -> color.hasSameName(name))
                .findFirst();

        if (existing.isEmpty()) {
            return addColor(name, hex, displayOrder);
        }

        ProductColor color = existing.get();
        color.changeHex(hex);
        color.reorder(displayOrder);
        color.activate();

        return color;
    }

    /**
     * Creates the size, or updates and reactivates the one that already has that value.
     */
    public ProductSize upsertSize(String name, String value, int displayOrder) {
        Optional<ProductSize> existing = this.sizes.stream()
                .filter(size -> size.hasSameValue(value))
                .findFirst();

        if (existing.isEmpty()) {
            return addSize(name, value, displayOrder);
        }

        ProductSize size = existing.get();
        size.rename(name);
        size.reorder(displayOrder);
        size.activate();

        return size;
    }

    /**
     * Creates the variant of a color and size, or updates and reactivates the existing one.
     */
    public ProductVariant upsertVariant(
            ProductColor color,
            ProductSize size,
            String sku,
            int stock,
            BigDecimal priceOverride,
            boolean active
    ) {
        assertColorBelongsToProduct(color);
        assertSizeBelongsToProduct(size);

        Optional<ProductVariant> existing = this.variants.stream()
                .filter(variant -> variant.matches(color, size))
                .findFirst();

        ProductVariant variant;

        if (existing.isPresent()) {
            variant = existing.get();
            variant.changeSku(sku);
            variant.changeStock(stock);
            variant.changePriceOverride(priceOverride);
        } else {
            variant = addVariant(color, size, sku, stock, priceOverride);
        }

        if (active) {
            variant.activate();
        } else {
            variant.deactivate();
        }

        return variant;
    }

    /**
     * Deactivates every color, size and variant that is not in the given sets, so removed
     * options disappear from the store without losing their history.
     */
    public void keepOnly(Collection<String> colorNames, Collection<String> sizeValues, Collection<ProductVariant> keptVariants) {
        this.colors.stream()
                .filter(color -> colorNames.stream().noneMatch(color::hasSameName))
                .toList()
                .forEach(this::deactivateColor);

        this.sizes.stream()
                .filter(size -> sizeValues.stream().noneMatch(size::hasSameValue))
                .toList()
                .forEach(this::deactivateSize);

        Set<ProductVariant> kept = Collections.newSetFromMap(new IdentityHashMap<>());
        kept.addAll(keptVariants);

        this.variants.stream()
                .filter(variant -> !kept.contains(variant))
                .forEach(ProductVariant::deactivate);
    }

    private void assertColorBelongsToProduct(ProductColor color) {
        Guard.notNull(color, ErrorCode.PRODUCT_COLOR_NOT_FOUND);

        Guard.isTrue(
                color.belongsTo(this),
                ErrorCode.PRODUCT_OPTION_DOES_NOT_BELONG,
                "color"
        );
    }

    private void assertSizeBelongsToProduct(ProductSize size) {
        Guard.notNull(size, ErrorCode.PRODUCT_SIZE_NOT_FOUND);

        Guard.isTrue(
                size.belongsTo(this),
                ErrorCode.PRODUCT_OPTION_DOES_NOT_BELONG,
                "size"
        );
    }
}