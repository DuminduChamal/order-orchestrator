package com.orderorchestrator.order.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        String productId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
