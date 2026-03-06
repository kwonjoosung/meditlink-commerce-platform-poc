package com.meditlink.poc.commerce.core.product.api.dto;

import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.product.ProductFeature;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleDeserializer;

import java.util.List;

public final class DtoAssembler {

    private DtoAssembler() {}

    public static ProductGroupDto toDto(ProductGroup pg, List<ProductDto> products) {
        return new ProductGroupDto(
                pg.getProductGroupId().toString(),
                pg.getSlug(),
                pg.getName(),
                pg.getDescription(),
                pg.getStatus().name(),
                pg.getDisplayOrder(),
                pg.getCondition() != null ? RuleDeserializer.serialize(pg.getCondition()) : null,
                pg.getAttributes(),
                pg.getMetadata(),
                pg.getTags(),
                products
        );
    }

    public static ProductGroupDto toDto(ProductGroup pg) {
        return toDto(pg, List.of());
    }

    public static ProductDto toDto(Product p, List<PriceDto> prices) {
        return new ProductDto(
                p.getProductId().toString(),
                p.getProductGroupId().toString(),
                p.getExternalId(),
                p.getName(),
                p.getDescription(),
                p.getType(),
                p.getBillingType(),
                p.getStatus().name(),
                p.getDisplayOrder(),
                p.getCondition() != null ? RuleDeserializer.serialize(p.getCondition()) : null,
                p.getAttributes(),
                p.getMetadata(),
                p.getTags(),
                p.getFeatures().stream().map(DtoAssembler::toDto).toList(),
                prices
        );
    }

    public static ProductDto toDto(Product p) {
        return toDto(p, List.of());
    }

    public static ProductFeatureDto toDto(ProductFeature f) {
        return new ProductFeatureDto(
                f.getProductId().toString(),
                f.getFeatureCode(),
                f.getQuota(),
                f.getAttributes()
        );
    }

    public static PriceDto toDto(Price p) {
        return new PriceDto(
                p.getPriceId().toString(),
                p.getProductId().toString(),
                p.getExternalId(),
                p.getCurrency(),
                p.getAmount(),
                p.getBillingInterval(),
                p.getIntervalCount(),
                p.isDefault(),
                p.getCondition() != null ? RuleDeserializer.serialize(p.getCondition()) : null,
                p.getAttributes(),
                p.getMetadata(),
                p.getTags()
        );
    }
}
