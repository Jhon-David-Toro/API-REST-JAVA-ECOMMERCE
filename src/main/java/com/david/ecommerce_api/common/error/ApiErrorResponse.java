package com.david.ecommerce_api.common.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldErrorResponse> fields
) {
    public record FieldErrorResponse(
            String field,
            String message
    ) {}
}
