package com.meditlink.poc.commerce.core.product.infrastructure.mapper;

import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.PriceEntity;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleDeserializer;

import java.util.Arrays;
import java.util.List;

public final class PriceMapper {

    private PriceMapper() {}

    public static Price toDomain(PriceEntity entity) {
        return Price.reconstitute(
                PriceId.of(entity.getPriceId()),
                ProductId.of(entity.getProductId()),
                entity.getExternalId(),
                entity.getCurrency(),
                entity.getAmount(),
                entity.getBillingInterval(),
                entity.getIntervalCount(),
                entity.isDefault(),
                entity.getCondition() != null ? RuleDeserializer.deserialize(entity.getCondition()) : null,
                entity.getAttributes(),
                entity.getMetadata(),
                entity.getTags() != null ? Arrays.asList(entity.getTags()) : List.of(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PriceEntity toEntity(Price domain) {
        var entity = new PriceEntity();
        entity.setPriceId(domain.getPriceId().value());
        entity.setProductId(domain.getProductId().value());
        entity.setExternalId(domain.getExternalId());
        entity.setCurrency(domain.getCurrency());
        entity.setAmount(domain.getAmount());
        entity.setBillingInterval(domain.getBillingInterval());
        entity.setIntervalCount(domain.getIntervalCount());
        entity.setDefault(domain.isDefault());
        entity.setCondition(domain.getCondition() != null ? RuleDeserializer.serialize(domain.getCondition()) : null);
        entity.setAttributes(domain.getAttributes());
        entity.setMetadata(domain.getMetadata());
        entity.setTags(domain.getTags().toArray(new String[0]));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
