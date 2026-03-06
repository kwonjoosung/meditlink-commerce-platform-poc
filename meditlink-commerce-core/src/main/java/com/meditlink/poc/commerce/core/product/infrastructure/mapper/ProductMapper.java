package com.meditlink.poc.commerce.core.product.infrastructure.mapper;

import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.product.ProductFeature;
import com.meditlink.poc.commerce.core.product.domain.product.ProductStatus;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductEntity;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductFeatureEntity;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleDeserializer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toDomain(ProductEntity entity, List<ProductFeatureEntity> featureEntities) {
        List<ProductFeature> features = featureEntities.stream()
                .map(fe -> new ProductFeature(
                        ProductId.of(fe.getProductId()),
                        fe.getFeatureCode(),
                        fe.getQuota(),
                        fe.getAttributes()
                ))
                .toList();

        return Product.reconstitute(
                ProductId.of(entity.getProductId()),
                ProductGroupId.of(entity.getProductGroupId()),
                entity.getExternalId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getBillingType(),
                ProductStatus.valueOf(entity.getStatus()),
                entity.getDisplayOrder(),
                entity.getCondition() != null ? RuleDeserializer.deserialize(entity.getCondition()) : null,
                entity.getAttributes(),
                entity.getMetadata(),
                entity.getTags() != null ? Arrays.asList(entity.getTags()) : List.of(),
                features,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductEntity toEntity(Product domain) {
        var entity = new ProductEntity();
        entity.setProductId(domain.getProductId().value());
        entity.setProductGroupId(domain.getProductGroupId().value());
        entity.setExternalId(domain.getExternalId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setType(domain.getType());
        entity.setBillingType(domain.getBillingType());
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

    public static List<ProductFeatureEntity> toFeatureEntities(Product domain) {
        return domain.getFeatures().stream()
                .map(f -> {
                    var fe = new ProductFeatureEntity();
                    fe.setProductId(domain.getProductId().value());
                    fe.setFeatureCode(f.getFeatureCode());
                    fe.setQuota(f.getQuota());
                    fe.setAttributes(f.getAttributes());
                    return fe;
                })
                .toList();
    }
}
