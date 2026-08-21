package com.david.ecommerce_api.product.domain;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "product_colors",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_color_name",
                        columnNames = {"product_id", "name"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductColor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 20)
    private String hex;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private ProductColor(String name, String hex, int displayOrder) {
        rename(name);
        changeHex(hex);
        this.displayOrder = displayOrder;
    }

    public static ProductColor create(String name, String hex, int displayOrder) {
        return new ProductColor(name, hex, displayOrder);
    }

    public void rename(String name) {
        Guard.notBlank(name, ErrorCode.VALIDATION_ERROR, "color_name");
        this.name = name.trim();
    }

    public void changeHex(String hex) {
        Guard.notBlank(hex, ErrorCode.VALIDATION_ERROR, "color_hex");
        this.hex = hex.trim();
    }

    public void reorder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    boolean hasSameName(String name) {
        return this.name.equalsIgnoreCase(name.trim());
    }

    boolean belongsTo(Product product) {
        if (this.product == product) {
            return true;
        }

        return this.product != null
                && product != null
                && this.product.getId() != null
                && this.product.getId().equals(product.getId());
    }

    void attachTo(Product product) {
        this.product = product;
    }

    void detach() {
        this.product = null;
    }
}