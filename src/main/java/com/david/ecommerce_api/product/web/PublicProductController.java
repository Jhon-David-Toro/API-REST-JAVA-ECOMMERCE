package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.common.dto.PageResponse;
import com.david.ecommerce_api.product.service.PublicProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
@Validated
public class PublicProductController {

    private final PublicProductService publicProductService;

    @GetMapping
    public PageResponse<ProductResponses.Card> getProducts(
            @RequestParam(required = false) String q,

            @RequestParam(required = false) Boolean onSale,

            @RequestParam(defaultValue = "latest") String sort,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "12")
            @Min(1)
            @Max(60)
            int size
    ) {
        return publicProductService.getProducts(
                q,
                onSale,
                sort,
                page,
                size
        );
    }

    @GetMapping("/new-arrivals")
    public PageResponse<ProductResponses.Card> getNewArrivals(
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "12")
            @Min(1)
            @Max(60)
            int size
    ) {
        return publicProductService.getNewArrivals(page, size);
    }

    @GetMapping("/top-selling")
    public PageResponse<ProductResponses.Card> getTopSelling(
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "12")
            @Min(1)
            @Max(60)
            int size
    ) {
        return publicProductService.getTopSelling(page, size);
    }

    @GetMapping("/on-sale")
    public PageResponse<ProductResponses.Card> getOnSale(
            @RequestParam(defaultValue = "latest") String sort,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "12")
            @Min(1)
            @Max(60)
            int size
    ) {
        return publicProductService.getOnSale(
                sort,
                page,
                size
        );
    }

    @GetMapping("/{slug}")
    public ProductResponses.Detail getProductDetail(
            @PathVariable String slug
    ) {
        return publicProductService.getProductDetail(slug);
    }

    @GetMapping("/{slug}/reviews")
    public PageResponse<ProductResponses.Review> getProductReviews(
            @PathVariable String slug,

            @RequestParam(defaultValue = "latest") String sort,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "6")
            @Min(1)
            @Max(50)
            int size
    ) {
        return publicProductService.getProductReviews(
                slug,
                sort,
                page,
                size
        );
    }
}