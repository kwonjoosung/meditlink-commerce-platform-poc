package com.meditlink.poc.commerce.core.product.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 토픽 자동 생성 설정.
 * 애플리케이션 시작 시 토픽이 없으면 자동으로 생성한다.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.product-created}")
    private String productCreatedTopic;

    @Value("${kafka.topic.product-updated}")
    private String productUpdatedTopic;

    @Value("${kafka.topic.price-changed}")
    private String priceChangedTopic;

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(productCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(productUpdatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic priceChangedTopic() {
        return TopicBuilder.name(priceChangedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
