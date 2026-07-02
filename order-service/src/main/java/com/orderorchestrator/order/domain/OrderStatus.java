package com.orderorchestrator.order.domain;

public enum OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    INVENTORY_FAILED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    COMPLETED,
    CANCELLED
}
