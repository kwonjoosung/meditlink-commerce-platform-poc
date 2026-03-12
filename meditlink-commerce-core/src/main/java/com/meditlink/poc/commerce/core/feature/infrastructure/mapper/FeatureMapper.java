package com.meditlink.poc.commerce.core.feature.infrastructure.mapper;

import com.meditlink.poc.commerce.core.feature.domain.Feature;
import com.meditlink.poc.commerce.core.feature.domain.FeatureStatus;
import com.meditlink.poc.commerce.core.feature.domain.FeatureType;
import com.meditlink.poc.commerce.core.feature.infrastructure.persistence.entity.FeatureEntity;

public final class FeatureMapper {

    private FeatureMapper() {}

    public static Feature toDomain(FeatureEntity entity) {
        return Feature.reconstitute(
                entity.getFeatureId(),
                entity.getFeatureCode(),
                entity.getName(),
                entity.getDescription(),
                FeatureType.valueOf(entity.getType()),
                FeatureStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static FeatureEntity toEntity(Feature domain) {
        var entity = new FeatureEntity();
        entity.setFeatureId(domain.getFeatureId());
        entity.setFeatureCode(domain.getFeatureCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setType(domain.getType().name());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
