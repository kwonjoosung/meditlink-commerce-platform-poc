package com.meditlink.poc.commerce.core.product.infrastructure.mapper;

import com.meditlink.poc.commerce.core.product.domain.product.*;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductEntity;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductFeatureEntity;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;

import java.util.Arrays;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toDomain(ProductEntity entity, List<ProductFeatureEntity> featureEntities) {
        List<ProductFeature> features = featureEntities.stream()
                .map(fe -> new ProductFeature(
                        ProductId.of(fe.getProductId()),
                        fe.getFeatureCode(),
                        fe.getQuota(),
                        fe.getDisplayLabel(),
                        fe.isHighlighted(),
                        fe.getStripeEntitlementId()
                ))
                .toList();

        return Product.reconstitute(
                ProductId.of(entity.getProductId()),
                entity.getProductGroupId() != null ? ProductGroupId.of(entity.getProductGroupId()) : null,
                entity.getExternalId(),
                entity.getName(),
                entity.getDisplayName(),
                entity.getDescription(),
                ItemType.valueOf(entity.getItemType()),
                ProductStatus.valueOf(entity.getStatus()),
                entity.getTierOrder(),
                Visibility.valueOf(entity.getVisibility()),
                entity.getDisplayConfig(),
                entity.getVisibilityRules(),
                entity.getCompatibility(),
                entity.getTags() != null ? Arrays.asList(entity.getTags()) : List.of(),
                features,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductEntity toEntity(Product domain) {
        var entity = new ProductEntity();
        entity.setProductId(domain.getProductId().value());
        entity.setProductGroupId(domain.getProductGroupId() != null ? domain.getProductGroupId().value() : null);
        entity.setExternalId(domain.getExternalId());
        entity.setName(domain.getName());
        entity.setDisplayName(domain.getDisplayName());
        entity.setDescription(domain.getDescription());
        entity.setItemType(domain.getItemType().name());
        entity.setStatus(domain.getStatus().name());
        entity.setTierOrder(domain.getTierOrder());
        entity.setVisibility(domain.getVisibility().name());
        entity.setDisplayConfig(domain.getDisplayConfig());
        entity.setVisibilityRules(domain.getVisibilityRules());
        entity.setCompatibility(domain.getCompatibility());
        entity.setTags(domain.getTags().toArray(new String[0]));
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static List<ProductFeatureEntity> toFeatureEntities(Product domain) {
        return domain.getFeatures().stream()
                .map(f -> {
                    var fe = new ProductFeatureEntity();
                    fe.setProductId(domain.getProductId().value());
                    fe.setFeatureCode(f.getFeatureCode());
                    fe.setQuota(f.getQuota());
                    fe.setDisplayLabel(f.getDisplayLabel());
                    fe.setHighlighted(f.isHighlighted());
                    fe.setStripeEntitlementId(f.getStripeEntitlementId());
                    return fe;
                })
                .toList();
    }
}
