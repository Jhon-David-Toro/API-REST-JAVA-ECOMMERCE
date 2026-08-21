package com.david.ecommerce_api.product.domain;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_features")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductFeature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private ProductFeature(String text, int displayOrder) {
        Guard.notBlank(text, ErrorCode.VALIDATION_ERROR, "feature_text");
        this.text = text;
        this.displayOrder = displayOrder;
    }

    public static ProductFeature create(String text, int displayOrder) {
        return new ProductFeature(text, displayOrder);
    }

    void attachTo(Product product) {
        this.product = product;
    }
}