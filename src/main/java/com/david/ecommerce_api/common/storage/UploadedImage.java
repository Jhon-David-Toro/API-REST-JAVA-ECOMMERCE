package com.david.ecommerce_api.common.storage;

public record UploadedImage(
        String secureUrl,
        String publicId,
        Integer width,
        Integer height
) {
}
