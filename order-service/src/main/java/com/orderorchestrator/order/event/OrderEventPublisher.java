package com.orderorchestrator.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${order-events.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Keys every event on the order id so all events for a given order land on the same
     * partition and are delivered in order - inventory-service and payment-service will
     * both depend on this later when they consume the same topic.
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = event.orderId().toString();

        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderCreated for order {}", event.orderId(), ex);
            } else {
                log.info("Published OrderCreated for order {} to partition {}",
                        event.orderId(), result.getRecordMetadata().partition());
            }
        });
    }
}
