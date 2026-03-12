package com.meditlink.poc.commerce.core.product.infrastructure.mapper;

import com.meditlink.poc.commerce.core.product.domain.price.BillingPeriod;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.PriceEntity;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;

public final class PriceMapper {

    private PriceMapper() {}

    public static Price toDomain(PriceEntity entity) {
        return Price.reconstitute(
                PriceId.of(entity.getPriceId()),
                ProductId.of(entity.getProductId()),
                entity.getExternalId(),
                entity.getCurrency(),
                entity.getAmount(),
                BillingPeriod.valueOf(entity.getBillingPeriod()),
                entity.isDefault(),
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
        entity.setBillingPeriod(domain.getBillingPeriod().name());
        entity.setDefault(domain.isDefault());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
