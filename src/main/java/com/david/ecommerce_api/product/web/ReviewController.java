package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.auth.AuthenticatedUser;
import com.david.ecommerce_api.product.domain.ReviewStatus;
import com.david.ecommerce_api.product.service.ReviewService;
import com.david.ecommerce_api.product.web.ReviewDtos.CreateReviewRequest;
import com.david.ecommerce_api.product.web.ReviewDtos.ReviewResponse;
import com.david.ecommerce_api.product.web.ReviewDtos.StatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/public/catalog/{productId}/reviews")
    public List<ReviewResponse> approved(@PathVariable Long productId) {
        return reviewService.listApproved(productId).stream().map(ReviewResponse::from).toList();
    }

    @PostMapping("/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ReviewResponse.from(reviewService.create(user, productId, request));
    }

    @GetMapping("/admin/reviews")
    public List<ReviewResponse> all(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String status
    ) {
        ReviewStatus filter = status == null || status.isBlank() ? null : ReviewStatus.valueOf(status.toUpperCase(Locale.ROOT));

        return reviewService.listAll(productId, filter).stream().map(ReviewResponse::from).toList();
    }

    @PatchMapping("/admin/reviews/{id}/status")
    public ReviewResponse moderate(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return ReviewResponse.from(reviewService.moderate(id, ReviewStatus.valueOf(request.status().toUpperCase(Locale.ROOT))));
    }

    @DeleteMapping("/admin/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }
}
