package com.david.ecommerce_api.order;

import com.david.ecommerce_api.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line of an order: a snapshot of the product at the moment of the purchase, so later edits do not change it.
 */
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 1000)
    private String image;

    @Column(length = 120)
    private String sku;

    @Column(length = 80)
    private String color;

    @Column(length = 80)
    private String size;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    public OrderItem(
            Long productId,
            Long variantId,
            String name,
            String image,
            String sku,
            String color,
            String size,
            BigDecimal unitPrice,
            int quantity
    ) {
        this.productId = productId;
        this.variantId = variantId;
        this.name = name;
        this.image = image;
        this.sku = sku;
        this.color = color;
        this.size = size;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    void attachTo(Order order) {
        this.order = order;
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
