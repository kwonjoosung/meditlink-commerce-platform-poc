package com.meditlink.poc.commerce.core.product.infrastructure.kafka;

import com.meditlink.poc.commerce.common.proto.v1.PriceChangedEvent;
import com.meditlink.poc.commerce.common.proto.v1.ProductCreatedEvent;
import com.meditlink.poc.commerce.common.proto.v1.ProductUpdatedEvent;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String productCreatedTopic;
    private final String productUpdatedTopic;
    private final String priceChangedTopic;

    public ProductEventPublisher(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            @Value("${kafka.topic.product-created}") String productCreatedTopic,
            @Value("${kafka.topic.product-updated}") String productUpdatedTopic,
            @Value("${kafka.topic.price-changed}") String priceChangedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.productCreatedTopic = productCreatedTopic;
        this.productUpdatedTopic = productUpdatedTopic;
        this.priceChangedTopic = priceChangedTopic;
    }

    public void publishProductCreated(Product product) {
        var event = ProductCreatedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setProductId(product.getProductId().value().toString())
                .setProductGroupId(product.getProductGroupId().value().toString())
                .setName(product.getName())
                .setDescription(product.getDescription() != null ? product.getDescription() : "")
                .setType(product.getItemType().name())
                .setBillingType(product.getItemType().name())
                .setExternalId(product.getExternalId() != null ? product.getExternalId() : "")
                .setOccurredAt(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(productCreatedTopic, product.getProductId().value().toString(), event.toByteArray())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("ProductCreatedEvent 발행 실패: productId={}", product.getProductId().value(), ex);
                    } else {
                        log.info("ProductCreatedEvent 발행 완료: productId={}, offset={}",
                                product.getProductId().value(), result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishProductUpdated(Product product) {
        var event = ProductUpdatedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setProductId(product.getProductId().value().toString())
                .setName(product.getName())
                .setDescription(product.getDescription() != null ? product.getDescription() : "")
                .setActive(product.getStatus() == com.meditlink.poc.commerce.core.product.domain.product.ProductStatus.ACTIVE)
                .setOccurredAt(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(productUpdatedTopic, product.getProductId().value().toString(), event.toByteArray())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("ProductUpdatedEvent 발행 실패: productId={}", product.getProductId().value(), ex);
                    } else {
                        log.info("ProductUpdatedEvent 발행 완료: productId={}, offset={}",
                                product.getProductId().value(), result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishPriceChanged(Price price, PriceChangedEvent.ChangeType changeType) {
        var event = PriceChangedEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setPriceId(price.getPriceId().value().toString())
                .setProductId(price.getProductId().value().toString())
                .setCurrency(price.getCurrency())
                .setAmount(price.getAmount())
                .setBillingInterval(price.getBillingPeriod().name())
                .setIntervalCount(1)
                .setIsDefault(price.isDefault())
                .setExternalId(price.getExternalId() != null ? price.getExternalId() : "")
                .setChangeType(changeType)
                .setOccurredAt(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(priceChangedTopic, price.getProductId().value().toString(), event.toByteArray())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("PriceChangedEvent 발행 실패: priceId={}", price.getPriceId().value(), ex);
                    } else {
                        log.info("PriceChangedEvent 발행 완료: priceId={}, offset={}",
                                price.getPriceId().value(), result.getRecordMetadata().offset());
                    }
                });
    }
}
