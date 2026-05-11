package com.example.ordering.enums;

public enum OrderStatus {

    PENDING,
    PREPARING,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus target) {
        switch (this) {
            case PENDING:
                return target == PREPARING || target == CANCELLED;
            case PREPARING:
                return target == COMPLETED;
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
        // 兼容旧状态映射
        if ("CONFIRMED".equalsIgnoreCase(value)) return PENDING;
        if ("READY".equalsIgnoreCase(value)) return PREPARING;
        if ("DELIVERED".equalsIgnoreCase(value)) return COMPLETED;
        throw new IllegalArgumentException("无效的订单状态: " + value);
    }
}
