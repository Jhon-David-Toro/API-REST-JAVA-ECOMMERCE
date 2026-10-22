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
        name = "product_reviews",
        indexes = {
                @Index(name = "idx_product_reviews_product", columnList = "product_id"),
                @Index(name = "idx_product_reviews_approved", columnList = "approved"),
                @Index(name = "idx_product_reviews_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 2000)
    private String comment;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean approved = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    private ReviewStatus reviewStatus;

    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private ProductReview(
            Product product,
            String customerName,
            int rating,
            String comment,
            boolean verified
    ) {
        Guard.notNull(product, ErrorCode.RESOURCE_NOT_FOUND, "product");
        Guard.notBlank(customerName, ErrorCode.VALIDATION_ERROR, "customerName");
        Guard.notBlank(comment, ErrorCode.VALIDATION_ERROR, "comment");
        Guard.isTrue(rating >= 1 && rating <= 5, ErrorCode.VALIDATION_ERROR, "rating");

        this.product = product;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.verified = verified;
        this.approved = false;
    }

    public static ProductReview create(
            Product product,
            String customerName,
            int rating,
            String comment,
            boolean verified
    ) {
        return new ProductReview(product, customerName, rating, comment, verified);
    }

    public ReviewStatus getStatus() {
        if (this.reviewStatus != null) {
            return this.reviewStatus;
        }

        return this.approved ? ReviewStatus.APPROVED : ReviewStatus.PENDING;
    }

    public void writtenBy(Long userId) {
        this.userId = userId;
    }

    public void approve() {
        this.approved = true;
        this.reviewStatus = ReviewStatus.APPROVED;
    }

    public void reject() {
        this.approved = false;
        this.reviewStatus = ReviewStatus.REJECTED;
    }

    public void markPending() {
        this.approved = false;
        this.reviewStatus = ReviewStatus.PENDING;
    }
}