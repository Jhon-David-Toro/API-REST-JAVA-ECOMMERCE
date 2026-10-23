package com.david.ecommerce_api.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class BrandDtos {

    private BrandDtos() {
    }

    public record BrandRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 140) String slug,
            @Size(max = 1000) String logo,
            Boolean visible
    ) {
    }

    public record BrandResponse(
            Long id,
            String name,
            String slug,
            String logo,
            boolean visible
    ) {
        public static BrandResponse from(Brand brand) {
            return new BrandResponse(
                    brand.getId(),
                    brand.getName(),
                    brand.getSlug(),
                    brand.getLogo(),
                    brand.isVisible()
            );
        }
    }
}
