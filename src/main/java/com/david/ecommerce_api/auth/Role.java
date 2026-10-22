package com.david.ecommerce_api.auth;

public enum Role {
    ADMIN,
    CUSTOMER;

    public String authority() {
        return "ROLE_" + name();
    }

    public String apiValue() {
        return name().toLowerCase();
    }
}
