package com.david.ecommerce_api.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class PageDtos {

    private PageDtos() {
    }

    public record PageRequest(
            @NotBlank @Size(max = 180) String title,
            @Size(max = 200) String slug,
            @Size(max = 50000) String body,
            Boolean published
    ) {
    }

    public record PageResponse(
            Long id,
            String title,
            String slug,
            String body,
            boolean published,
            long updatedAt
    ) {
        public static PageResponse from(StaticPage page) {
            return new PageResponse(
                    page.getId(),
                    page.getTitle(),
                    page.getSlug(),
                    page.getBody(),
                    page.isPublished(),
                    page.getEditedAt().toEpochMilli()
            );
        }
    }
}
