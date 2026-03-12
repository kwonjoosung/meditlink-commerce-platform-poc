package com.meditlink.poc.commerce.core.product.api.dto;

import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.product.ProductFeature;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;

import java.util.List;

public final class DtoAssembler {

    private DtoAssembler() {}

    public static ProductGroupDto toDto(ProductGroup pg, List<ProductDto> products) {
        return new ProductGroupDto(
                pg.getProductGroupId().toString(),
                pg.getSlug(),
                pg.getName(),
                pg.getDescription(),
                pg.getType().name(),
                pg.getStatus().name(),
                pg.getSortOrder(),
                pg.getDisplayConfig(),
                pg.getVisibilityRules(),
                products
        );
    }

    public static ProductGroupDto toDto(ProductGroup pg) {
        return toDto(pg, List.of());
    }

    public static ProductDto toDto(Product p, List<PriceDto> prices) {
        return new ProductDto(
                p.getProductId().toString(),
                p.getProductGroupId() != null ? p.getProductGroupId().toString() : null,
                p.getExternalId(),
                p.getName(),
                p.getDisplayName(),
                p.getDescription(),
                p.getItemType().name(),
                p.getStatus().name(),
                p.getTierOrder(),
                p.getVisibility().name(),
                p.getDisplayConfig(),
                p.getVisibilityRules(),
                p.getCompatibility(),
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
                f.getDisplayLabel(),
                f.isHighlighted()
        );
    }

    public static PriceDto toDto(Price p) {
        return new PriceDto(
                p.getPriceId().toString(),
                p.getProductId().toString(),
                p.getExternalId(),
                p.getCurrency(),
                p.getAmount(),
                p.getBillingPeriod().name(),
                p.isDefault()
        );
    }
}
