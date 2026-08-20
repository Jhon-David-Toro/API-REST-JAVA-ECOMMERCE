package com.david.ecommerce_api.common.validation;

import com.david.ecommerce_api.common.error.DomainException;
import com.david.ecommerce_api.common.error.ErrorCode;

import java.math.BigDecimal;

public final class Guard {

    private Guard() {
    }

    public static void isTrue(boolean condition, ErrorCode errorCode, Object... args) {
        if (!condition) {
            throw new DomainException(errorCode, args);
        }
    }

    public static void isFalse(boolean condition, ErrorCode errorCode, Object... args) {
        if (condition) {
            throw new DomainException(errorCode, args);
        }
    }

    public static void notNull(Object value, ErrorCode errorCode, Object... args) {
        if (value == null) {
            throw new DomainException(errorCode, args);
        }
    }

    public static void notBlank(String value, ErrorCode errorCode, Object... args) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainException(errorCode, args);
        }
    }

    public static void positive(BigDecimal value, ErrorCode errorCode, Object... args) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException(errorCode, args);
        }
    }

    public static void positiveOrZero(int value, ErrorCode errorCode, Object... args) {
        if (value < 0) {
            throw new DomainException(errorCode, args);
        }
    }

    public static void positiveQuantity(int value) {
        if (value <= 0) {
            throw new DomainException(ErrorCode.STOCK_INVALID_QUANTITY, value);
        }
    }
}