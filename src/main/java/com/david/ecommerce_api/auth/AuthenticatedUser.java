package com.david.ecommerce_api.auth;

public record AuthenticatedUser(
        Long id,
        String email,
        Role role
) {
}
