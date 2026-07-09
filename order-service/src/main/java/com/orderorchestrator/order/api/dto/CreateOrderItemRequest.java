package com.orderorchestrator.order.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotBlank String productId,
        @NotNull @Min(1) Integer quantity,
        @NotNull @Positive BigDecimal unitPrice
) {
}
