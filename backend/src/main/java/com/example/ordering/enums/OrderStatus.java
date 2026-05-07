package com.example.ordering.enums;

public enum OrderStatus {

    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        switch (this) {
            case PENDING:
                return target == CONFIRMED || target == CANCELLED;
            case CONFIRMED:
                return target == PREPARING || target == CANCELLED;
            case PREPARING:
                return target == READY || target == CANCELLED;
            case READY:
                return target == DELIVERED;
            default:
                return false;
        }
    }

    public static OrderStatus fromString(String value) {
        for (OrderStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("无效的订单状态: " + value);
    }
}
