package com.david.ecommerce_api.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    IMAGE_INVALID_FILE(
            "image.invalid_file",
            HttpStatus.BAD_REQUEST
    ),

    IMAGE_UPLOAD_FAILED(
            "image.upload_failed",
            HttpStatus.BAD_GATEWAY
    ),

    IMAGE_DELETE_FAILED(
            "image.delete_failed",
            HttpStatus.BAD_GATEWAY
    ),

    PRODUCT_IMAGE_NOT_FOUND(
            "product.image_not_found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_INVALID_PRICE(
            "product.invalid_price",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_INVALID_COMPARE_AT_PRICE(
            "product.invalid_compare_at_price",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_INVALID_STATUS_TRANSITION(
            "product.invalid_status_transition",
            HttpStatus.CONFLICT
    ),

    PRODUCT_NOT_PUBLISHABLE(
            "product.not_publishable",
            HttpStatus.CONFLICT
    ),

    PRODUCT_COLOR_NOT_FOUND(
            "product.color_not_found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_NOT_FOUND(
            "product.not_found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_SIZE_NOT_FOUND(
            "product.size_not_found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_OPTION_DOES_NOT_BELONG(
            "product.option_does_not_belong",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_VARIANT_DUPLICATED(
            "product.variant_duplicated",
            HttpStatus.CONFLICT
    ),

    PRODUCT_VARIANT_NOT_FOUND(
            "product.variant_not_found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_VARIANT_INACTIVE(
            "product.variant_inactive",
            HttpStatus.CONFLICT
    ),

    STOCK_INVALID_QUANTITY(
            "stock.invalid_quantity",
            HttpStatus.BAD_REQUEST
    ),

    STOCK_INSUFFICIENT(
            "stock.insufficient",
            HttpStatus.CONFLICT
    ),

    AUTH_INVALID_CREDENTIALS(
            "auth.invalid_credentials",
            HttpStatus.UNAUTHORIZED
    ),

    AUTH_EMAIL_TAKEN(
            "auth.email_taken",
            HttpStatus.CONFLICT
    ),

    AUTH_ACCOUNT_BLOCKED(
            "auth.account_blocked",
            HttpStatus.FORBIDDEN
    ),

    AUTH_WRONG_PASSWORD(
            "auth.wrong_password",
            HttpStatus.BAD_REQUEST
    ),

    USER_NOT_FOUND(
            "user.not_found",
            HttpStatus.NOT_FOUND
    ),

    USER_ADMIN_CANNOT_BE_BLOCKED(
            "user.admin_cannot_be_blocked",
            HttpStatus.CONFLICT
    ),

    TAXONOMY_SLUG_TAKEN(
            "taxonomy.slug_taken",
            HttpStatus.CONFLICT
    ),

    CATEGORY_NOT_FOUND(
            "category.not_found",
            HttpStatus.NOT_FOUND
    ),

    BRAND_NOT_FOUND(
            "brand.not_found",
            HttpStatus.NOT_FOUND
    ),

    PRODUCT_SLUG_TAKEN(
            "product.slug_taken",
            HttpStatus.CONFLICT
    ),

    PRODUCT_NOT_DELETABLE(
            "product.not_deletable",
            HttpStatus.CONFLICT
    ),

    DATA_CONFLICT(
            "data.conflict",
            HttpStatus.CONFLICT
    ),

    PROMO_NOT_FOUND(
            "promo.not_found",
            HttpStatus.NOT_FOUND
    ),

    PROMO_DUPLICATED(
            "promo.duplicated",
            HttpStatus.BAD_REQUEST
    ),

    ORDER_NOT_FOUND(
            "order.not_found",
            HttpStatus.NOT_FOUND
    ),

    ORDER_INVALID_STATUS_TRANSITION(
            "order.invalid_status_transition",
            HttpStatus.CONFLICT
    ),

    ORDER_PRODUCT_UNAVAILABLE(
            "order.product_unavailable",
            HttpStatus.CONFLICT
    ),

    REVIEW_NOT_FOUND(
            "review.not_found",
            HttpStatus.NOT_FOUND
    ),

    VALIDATION_ERROR(
            "validation.error",
            HttpStatus.BAD_REQUEST
    ),

    RESOURCE_NOT_FOUND(
            "resource.not_found",
            HttpStatus.NOT_FOUND
    );

    private final String messageKey;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    public String messageKey() {
        return messageKey;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}