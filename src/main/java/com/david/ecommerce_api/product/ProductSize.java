package com.david.ecommerce_api.product;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
        import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "product_sizes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_size_value",
                        columnNames = {"product_id", "value"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSize extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 20)
    private String value;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private ProductSize(String name, String value, int displayOrder) {
        rename(name);
        changeValue(value);
        this.displayOrder = displayOrder;
    }

    public static ProductSize create(String name, String value, int displayOrder) {
        return new ProductSize(name, value, displayOrder);
    }

    public void rename(String name) {
        Guard.notBlank(name, ErrorCode.VALIDATION_ERROR, "size_name");
        this.name = name.trim();
    }

    public void changeValue(String value) {
        Guard.notBlank(value, ErrorCode.VALIDATION_ERROR, "size_value");
        this.value = value.trim().toUpperCase();
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

    boolean hasSameValue(String value) {
        return this.value.equalsIgnoreCase(value.trim());
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