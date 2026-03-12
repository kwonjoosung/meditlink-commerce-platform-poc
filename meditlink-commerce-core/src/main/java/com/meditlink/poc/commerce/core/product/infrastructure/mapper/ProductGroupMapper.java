package com.meditlink.poc.commerce.core.product.infrastructure.mapper;

import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupStatus;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupType;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductGroupEntity;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;

import java.util.Arrays;
import java.util.List;

public final class ProductGroupMapper {

    private ProductGroupMapper() {}

    public static ProductGroup toDomain(ProductGroupEntity entity) {
        return ProductGroup.reconstitute(
                ProductGroupId.of(entity.getProductGroupId()),
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                ProductGroupType.valueOf(entity.getType()),
                ProductGroupStatus.valueOf(entity.getStatus()),
                entity.getSortOrder(),
                entity.getDisplayConfig(),
                entity.getVisibilityRules(),
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
        entity.setType(domain.getType().name());
        entity.setStatus(domain.getStatus().name());
        entity.setSortOrder(domain.getSortOrder());
        entity.setDisplayConfig(domain.getDisplayConfig());
        entity.setVisibilityRules(domain.getVisibilityRules());
        entity.setTags(domain.getTags().toArray(new String[0]));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
