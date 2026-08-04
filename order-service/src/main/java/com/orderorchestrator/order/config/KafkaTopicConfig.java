package com.orderorchestrator.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the order-events topic explicitly rather than relying on
 * auto.create.topics.enable, which most real clusters turn off - auto-created topics get
 * the broker defaults (often 1 partition), which is rarely what you actually want.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderEventsTopic(@Value("${order-events.topic}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
