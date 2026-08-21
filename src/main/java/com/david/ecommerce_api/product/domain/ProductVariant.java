package com.david.ecommerce_api.product.domain;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Locale;

@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_variant_sku",
                        columnNames = {"sku"}
                ),
                @UniqueConstraint(
                        name = "uk_product_variant_product_color_size",
                        columnNames = {"product_id", "color_id", "size_id"}
                )
        },
        indexes = {
                @Index(name = "idx_product_variants_product", columnList = "product_id"),
                @Index(name = "idx_product_variants_sku", columnList = "sku"),
                @Index(name = "idx_product_variants_stock", columnList = "stock")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String sku;

    @Column(name = "price_override", precision = 10, scale = 2)
    private BigDecimal priceOverride;

    @Column(nullable = false)
    private int stock = 0;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private ProductColor color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false)
    private ProductSize size;

    private ProductVariant(
            Product product,
            ProductColor color,
            ProductSize size,
            String sku,
            int initialStock,
            BigDecimal priceOverride
    ) {
        Guard.notNull(product, ErrorCode.VALIDATION_ERROR, "product");
        Guard.notNull(color, ErrorCode.PRODUCT_COLOR_NOT_FOUND);
        Guard.notNull(size, ErrorCode.PRODUCT_SIZE_NOT_FOUND);
        Guard.notBlank(sku, ErrorCode.VALIDATION_ERROR, "sku");
        Guard.positiveOrZero(initialStock, ErrorCode.STOCK_INVALID_QUANTITY, initialStock);

        if (priceOverride != null) {
            Guard.positive(priceOverride, ErrorCode.PRODUCT_INVALID_PRICE);
        }

        this.product = product;
        this.color = color;
        this.size = size;
        this.sku = sku.trim().toUpperCase(Locale.ROOT);
        this.stock = initialStock;
        this.priceOverride = priceOverride;
    }

    public static ProductVariant create(
            Product product,
            ProductColor color,
            ProductSize size,
            String sku,
            int initialStock,
            BigDecimal priceOverride
    ) {
        return new ProductVariant(product, color, size, sku, initialStock, priceOverride);
    }

    public boolean matches(ProductColor color, ProductSize size) {
        return hasColor(color) && hasSize(size);
    }

    public boolean hasColor(ProductColor color) {
        return this.color.equals(color);
    }

    public boolean hasSize(ProductSize size) {
        return this.size.equals(size);
    }

    public BigDecimal getEffectivePrice() {
        return this.priceOverride != null
                ? this.priceOverride
                : this.product.getPrice();
    }

    public boolean isAvailable() {
        return this.active && this.stock > 0;
    }

    public void changeSku(String sku) {
        Guard.notBlank(sku, ErrorCode.VALIDATION_ERROR, "sku");
        this.sku = sku.trim().toUpperCase(Locale.ROOT);
    }

    public void changePriceOverride(BigDecimal priceOverride) {
        if (priceOverride != null) {
            Guard.positive(priceOverride, ErrorCode.PRODUCT_INVALID_PRICE);
        }

        this.priceOverride = priceOverride;
    }

    public void changeStock(int newStock) {
        Guard.positiveOrZero(newStock, ErrorCode.STOCK_INVALID_QUANTITY, newStock);
        this.stock = newStock;
    }

    public void increaseStock(int quantity) {
        Guard.positiveQuantity(quantity);
        this.stock += quantity;
    }

    public void decreaseStock(int quantity) {
        Guard.positiveQuantity(quantity);

        Guard.isTrue(
                this.stock >= quantity,
                ErrorCode.STOCK_INSUFFICIENT,
                this.sku,
                this.stock,
                quantity
        );

        this.stock -= quantity;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    void detach() {
        this.product = null;
    }
}