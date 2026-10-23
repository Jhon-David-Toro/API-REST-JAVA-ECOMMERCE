package com.david.ecommerce_api.product.service;

import com.david.ecommerce_api.auth.AuthenticatedUser;
import com.david.ecommerce_api.auth.User;
import com.david.ecommerce_api.auth.UserRepository;
import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;
import com.david.ecommerce_api.order.OrderRepository;
import com.david.ecommerce_api.product.domain.Product;
import com.david.ecommerce_api.product.domain.ProductReview;
import com.david.ecommerce_api.product.domain.ProductStatus;
import com.david.ecommerce_api.product.domain.ReviewStatus;
import com.david.ecommerce_api.product.repository.ProductRepository;
import com.david.ecommerce_api.product.repository.ProductReviewRepository;
import com.david.ecommerce_api.product.web.ReviewDtos.CreateReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final double RATING_SCALE = 10.0;
    private static final int MAX_LATEST = 20;

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * Stores a review that waits for moderation. It is marked as verified when the author bought the product.
     */
    @Transactional
    public ProductReview create(AuthenticatedUser author, Long productId, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .filter(Product::isPublished)
                .orElseThrow(() -> new DomainException(ErrorCode.PRODUCT_NOT_FOUND));
        User user = userRepository.findById(author.id())
                .orElseThrow(() -> new DomainException(ErrorCode.USER_NOT_FOUND));

        ProductReview review = ProductReview.create(
                product,
                user.getName(),
                request.rating(),
                request.text().trim(),
                orderRepository.hasPurchased(user.getId(), productId)
        );
        review.writtenBy(user.getId());

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<ProductReview> listApproved(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrueOrderByIdDesc(productId);
    }

    /**
     * Newest approved reviews of published products, for the home page.
     */
    @Transactional(readOnly = true)
    public List<ProductReview> listLatestApproved(int limit) {
        int size = Math.max(1, Math.min(limit, MAX_LATEST));

        return reviewRepository.findByApprovedTrueAndProductStatusOrderByIdDesc(ProductStatus.PUBLISHED, PageRequest.of(0, size));
    }

    @Transactional(readOnly = true)
    public List<ProductReview> listAll(Long productId, ReviewStatus status) {
        List<ProductReview> reviews = productId == null
                ? reviewRepository.findAllByOrderByIdDesc()
                : reviewRepository.findByProductIdOrderByIdDesc(productId);

        return status == null ? reviews : reviews.stream().filter(review -> review.getStatus() == status).toList();
    }

    @Transactional
    public ProductReview moderate(Long id, ReviewStatus status) {
        ProductReview review = find(id);

        switch (status) {
            case APPROVED -> review.approve();
            case REJECTED -> review.reject();
            case PENDING -> review.markPending();
        }

        syncRating(review.getProduct());

        return review;
    }

    @Transactional
    public void delete(Long id) {
        ProductReview review = find(id);
        Product product = review.getProduct();

        reviewRepository.delete(review);
        reviewRepository.flush();
        syncRating(product);
    }

    /**
     * Keeps the rating and the review count of a product in line with its approved reviews.
     */
    private void syncRating(Product product) {
        List<ProductReview> approved = reviewRepository.findByProductIdAndApprovedTrue(product.getId());
        double average = approved.stream().mapToInt(ProductReview::getRating).average().orElse(0.0);

        product.updateRating(Math.round(average * RATING_SCALE) / RATING_SCALE, approved.size());
    }

    private ProductReview find(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.REVIEW_NOT_FOUND));
    }
}
