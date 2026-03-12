package com.meditlink.poc.commerce.integration.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.meditlink.poc.commerce.common.proto.v1.PriceChangedEvent;
import com.meditlink.poc.commerce.common.proto.v1.ProductCreatedEvent;
import com.meditlink.poc.commerce.common.proto.v1.ProductUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Core 모듈에서 발행한 Product 도메인 이벤트를 수신하는 Consumer.
 * Protobuf byte[]를 역직렬화하여 처리한다.
 */
@Component
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    @KafkaListener(topics = "${kafka.topic.product-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onProductCreated(byte[] payload) {
        try {
            var event = ProductCreatedEvent.parseFrom(payload);
            log.info("[Kafka] ProductCreatedEvent 수신: productId={}, name={}, type={}, externalId={}",
                    event.getProductId(), event.getName(), event.getType(), event.getExternalId());

            // TODO: Client 측 비즈니스 로직 (캐시 갱신, 알림 등)
        } catch (InvalidProtocolBufferException e) {
            log.error("[Kafka] ProductCreatedEvent 역직렬화 실패", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.product-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onProductUpdated(byte[] payload) {
        try {
            var event = ProductUpdatedEvent.parseFrom(payload);
            log.info("[Kafka] ProductUpdatedEvent 수신: productId={}, name={}, active={}",
                    event.getProductId(), event.getName(), event.getActive());

            // TODO: Client 측 비즈니스 로직
        } catch (InvalidProtocolBufferException e) {
            log.error("[Kafka] ProductUpdatedEvent 역직렬화 실패", e);
        }
    }

    @KafkaListener(topics = "${kafka.topic.price-changed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPriceChanged(byte[] payload) {
        try {
            var event = PriceChangedEvent.parseFrom(payload);
            log.info("[Kafka] PriceChangedEvent 수신: priceId={}, productId={}, changeType={}, amount={} {}",
                    event.getPriceId(), event.getProductId(), event.getChangeType(),
                    event.getAmount(), event.getCurrency());

            // TODO: Client 측 비즈니스 로직
        } catch (InvalidProtocolBufferException e) {
            log.error("[Kafka] PriceChangedEvent 역직렬화 실패", e);
        }
    }
}
