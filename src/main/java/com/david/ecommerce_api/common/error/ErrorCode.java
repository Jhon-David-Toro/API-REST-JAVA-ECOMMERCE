package com.david.ecommerce_api.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

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