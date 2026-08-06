package com.orderorchestrator.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The wire contract published to the {@code order-events} topic when an order is created.
 * This is a deliberately separate type from the {@code Order} JPA entity and from the API's
 * {@code OrderResponse} - the DB schema, the public REST contract and the Kafka event contract
 * are three different things that will each evolve on their own schedule.
 */
public record OrderCreatedEvent(
        UUID orderId,
        String customerId,
        List<OrderedItem> items,
        BigDecimal totalAmount,
        Instant occurredAt
) {
    public record OrderedItem(String productId, Integer quantity, BigDecimal unitPrice) {
    }
}
