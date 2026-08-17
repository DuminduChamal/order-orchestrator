package com.orderorchestrator.order.service;

import com.orderorchestrator.order.api.dto.CreateOrderItemRequest;
import com.orderorchestrator.order.api.dto.CreateOrderRequest;
import com.orderorchestrator.order.domain.Order;
import com.orderorchestrator.order.domain.OrderItem;
import com.orderorchestrator.order.event.OrderCreatedEvent;
import com.orderorchestrator.order.event.OrderEventPublisher;
import com.orderorchestrator.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(this::toOrderItem)
                .toList();

        BigDecimal total = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(UUID.randomUUID(), request.customerId(), items, total);
        Order saved = orderRepository.save(order);

        // NOTE: publishing inside the same transaction is the simplest thing that works for
        // this phase, but it's a known gap - if the Kafka send fails after the DB commit (or
        // vice versa) the two go out of sync. The fix is the transactional outbox pattern,
        // which is on the roadmap once inventory-service exists and there's an actual reason
        // to feel that pain.
        eventPublisher.publishOrderCreated(toEvent(saved));

        return saved;
    }

    private OrderItem toOrderItem(CreateOrderItemRequest item) {
        return new OrderItem(item.productId(), item.quantity(), item.unitPrice());
    }

    private OrderCreatedEvent toEvent(Order order) {
        List<OrderCreatedEvent.OrderedItem> items = order.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderedItem(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        return new OrderCreatedEvent(
                order.getId(), order.getCustomerId(), items, order.getTotalAmount(), Instant.now());
    }
}
