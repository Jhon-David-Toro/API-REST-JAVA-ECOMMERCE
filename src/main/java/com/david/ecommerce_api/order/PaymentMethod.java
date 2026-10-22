package com.david.ecommerce_api.order;

import java.util.Locale;

public enum PaymentMethod {
    CASH_ON_DELIVERY,
    BANK_TRANSFER;

    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PaymentMethod fromApi(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
