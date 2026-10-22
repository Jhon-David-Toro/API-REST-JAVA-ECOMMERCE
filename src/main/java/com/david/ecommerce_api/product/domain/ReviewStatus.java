package com.david.ecommerce_api.product.domain;

public enum ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public String apiValue() {
        return name().toLowerCase();
    }
}
