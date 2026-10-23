package com.david.ecommerce_api.order;

import java.util.Set;

public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public Set<OrderStatus> next() {
        return switch (this) {
            case PENDING -> Set.of(PAID, CANCELLED);
            case PAID -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED -> Set.of(DELIVERED, CANCELLED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }

    public boolean canMoveTo(OrderStatus target) {
        return next().contains(target);
    }

    public String apiValue() {
        return name().toLowerCase();
    }
}
