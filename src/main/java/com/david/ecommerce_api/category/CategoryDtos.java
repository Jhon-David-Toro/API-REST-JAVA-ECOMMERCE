package com.david.ecommerce_api.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record CategoryRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 140) String slug,
            @Size(max = 500) String description,
            @Size(max = 1000) String image,
            Boolean visible
    ) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String slug,
            String description,
            String image,
            boolean visible
    ) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getSlug(),
                    category.getDescription(),
                    category.getImage(),
                    category.isVisible()
            );
        }
    }
}
