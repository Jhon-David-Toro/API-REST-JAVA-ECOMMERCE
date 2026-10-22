package com.david.ecommerce_api.product.web;

import com.david.ecommerce_api.product.domain.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class AdminProductDtos {

    private AdminProductDtos() {
    }

    public record ImageInput(
            @NotBlank @Size(max = 1000) String imageUrl,
            @Size(max = 300) String publicId,
            @Size(max = 250) String alt,
            Boolean main,
            Integer width,
            Integer height
    ) {
    }

    public record ColorInput(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "must be a hex color like #1E3A8A") String hex
    ) {
    }

    public record SizeInput(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(max = 20) String value
    ) {
    }

    public record VariantInput(
            @NotBlank String color,
            @NotBlank String size,
            @NotBlank @Size(max = 120) String sku,
            @Min(0) int stock,
            @DecimalMin("0.01") BigDecimal priceOverride,
            Boolean active
    ) {
    }

    public record DetailInput(
            @Size(max = 3000) String overview,
            @Size(max = 1000) String origin,
            @Size(max = 180) String companyName,
            @Size(max = 3000) String aboutCompany,
            @Size(max = 2000) String careInstructions
    ) {
    }

    public record FaqInput(
            @NotBlank @Size(max = 500) String question,
            @NotBlank @Size(max = 2000) String answer
    ) {
    }

    public record ProductRequest(
            @NotBlank @Size(max = 180) String name,
            @Size(max = 220) String slug,
            @Size(max = 1000) String shortDescription,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            BigDecimal compareAtPrice,
            Long categoryId,
            Long brandId,
            @Size(max = 60) String dressStyle,
            @Valid List<ImageInput> images,
            @Valid List<ColorInput> colors,
            @Valid List<SizeInput> sizes,
            @Valid List<VariantInput> variants,
            @Valid DetailInput detail,
            List<@NotBlank @Size(max = 500) String> features,
            @Valid List<FaqInput> faqs
    ) {
    }

    public record Summary(
            Long id,
            String name,
            String slug,
            String thumbnailUrl,
            BigDecimal price,
            BigDecimal compareAtPrice,
            ProductStatus status,
            int stock,
            Long categoryId,
            String categoryName,
            Long brandId,
            String brandName,
            long createdAt
    ) {
    }

    public record ImageOutput(
            Long id,
            String imageUrl,
            String publicId,
            String alt,
            boolean main,
            int displayOrder,
            Integer width,
            Integer height
    ) {
    }

    public record ColorOutput(Long id, String name, String hex) {
    }

    public record SizeOutput(Long id, String name, String value) {
    }

    public record VariantOutput(
            Long id,
            String color,
            String size,
            String sku,
            int stock,
            BigDecimal priceOverride,
            boolean active
    ) {
    }

    public record DetailOutput(
            String overview,
            String origin,
            String companyName,
            String aboutCompany,
            String careInstructions
    ) {
    }

    public record FaqOutput(String question, String answer) {
    }

    public record Response(
            Long id,
            String name,
            String slug,
            String shortDescription,
            BigDecimal price,
            BigDecimal compareAtPrice,
            ProductStatus status,
            Long categoryId,
            String categoryName,
            Long brandId,
            String brandName,
            String dressStyle,
            double ratingAverage,
            int ratingCount,
            long totalSold,
            int stock,
            String thumbnailUrl,
            List<ImageOutput> images,
            List<ColorOutput> colors,
            List<SizeOutput> sizes,
            List<VariantOutput> variants,
            DetailOutput detail,
            List<String> features,
            List<FaqOutput> faqs,
            long createdAt,
            Long publishedAt
    ) {
    }
}
