package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.product.domain.ProductReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 2000) String text
    ) {
    }

    public record StatusRequest(
            @NotBlank @Pattern(regexp = "^(pending|approved|rejected)$", message = "is not a review status") String status
    ) {
    }

    public record ReviewResponse(
            Long id,
            Long productId,
            String author,
            int rating,
            String text,
            String date,
            boolean verified,
            String status
    ) {
        public static ReviewResponse from(ProductReview review) {
            return new ReviewResponse(
                    review.getId(),
                    review.getProduct().getId(),
                    review.getCustomerName(),
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt().toString(),
                    review.isVerified(),
                    review.getStatus().apiValue()
            );
        }
    }
}
