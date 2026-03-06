package com.meditlink.poc.commerce.core.product.infrastructure.mapper;

import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupStatus;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductGroupEntity;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleDeserializer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ProductGroupMapper {

    private ProductGroupMapper() {}

    public static ProductGroup toDomain(ProductGroupEntity entity) {
        return ProductGroup.reconstitute(
                ProductGroupId.of(entity.getProductGroupId()),
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                ProductGroupStatus.valueOf(entity.getStatus()),
                entity.getDisplayOrder(),
                entity.getCondition() != null ? RuleDeserializer.deserialize(entity.getCondition()) : null,
                entity.getAttributes(),
                entity.getMetadata(),
                entity.getTags() != null ? Arrays.asList(entity.getTags()) : List.of(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductGroupEntity toEntity(ProductGroup domain) {
        var entity = new ProductGroupEntity();
        entity.setProductGroupId(domain.getProductGroupId().value());
        entity.setSlug(domain.getSlug());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus().name());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setCondition(domain.getCondition() != null ? RuleDeserializer.serialize(domain.getCondition()) : null);
        entity.setAttributes(domain.getAttributes());
        entity.setMetadata(domain.getMetadata());
        entity.setTags(domain.getTags().toArray(new String[0]));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
