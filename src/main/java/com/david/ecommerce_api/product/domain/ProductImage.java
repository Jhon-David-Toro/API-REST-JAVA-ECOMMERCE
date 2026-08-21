package com.david.ecommerce_api.product.domain;

import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.common.persistence.BaseEntity;
import com.david.ecommerce_api.common.validation.Guard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "public_id", nullable = false, length = 300)
    private String publicId;

    @Column(length = 250)
    private String alt;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "main_image", nullable = false)
    private boolean mainImage = false;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private ProductImage(
            String imageUrl,
            String publicId,
            String alt,
            int displayOrder,
            boolean mainImage,
            Integer width,
            Integer height
    ) {
        Guard.notBlank(imageUrl, ErrorCode.VALIDATION_ERROR, "imageUrl");
        Guard.notBlank(publicId, ErrorCode.VALIDATION_ERROR, "publicId");

        this.imageUrl = imageUrl;
        this.publicId = publicId;
        this.alt = alt;
        this.displayOrder = displayOrder;
        this.mainImage = mainImage;
        this.width = width;
        this.height = height;
    }

    public static ProductImage create(
            String imageUrl,
            String publicId,
            String alt,
            int displayOrder,
            boolean mainImage,
            Integer width,
            Integer height
    ) {
        return new ProductImage(
                imageUrl,
                publicId,
                alt,
                displayOrder,
                mainImage,
                width,
                height
        );
    }

    void attachTo(Product product) {
        this.product = product;
    }

    void detach() {
        this.product = null;
    }

    void unmarkAsMain() {
        this.mainImage = false;
    }

    public void markAsMain() {
        this.mainImage = true;
    }

}
