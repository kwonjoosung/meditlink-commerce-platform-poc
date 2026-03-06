package com.meditlink.poc.commerce.core.product.api;

import com.meditlink.poc.commerce.core.product.api.dto.*;
import com.meditlink.poc.commerce.core.product.application.query.ProductQueryService;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductModuleApiImpl implements ProductModuleApi {

    private final ProductQueryService queryService;

    public ProductModuleApiImpl(ProductQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public List<ProductGroupDto> getActiveProductGroups() {
        return queryService.findAllProductGroups().stream()
                .filter(pg -> pg.getStatus() == ProductGroupStatus.ACTIVE)
                .map(pg -> {
                    var products = queryService.findProductsByProductGroupId(pg.getProductGroupId().toString());
                    var productDtos = products.stream().map(p -> {
                        var prices = queryService.findPricesByProductId(p.getProductId().toString());
                        var priceDtos = prices.stream().map(DtoAssembler::toDto).toList();
                        return DtoAssembler.toDto(p, priceDtos);
                    }).toList();
                    return DtoAssembler.toDto(pg, productDtos);
                })
                .toList();
    }

    @Override
    public List<ProductFeatureDto> getProductFeatures(String productId) {
        return queryService.findProductById(productId)
                .map(p -> p.getFeatures().stream().map(DtoAssembler::toDto).toList())
                .orElse(List.of());
    }

    @Override
    public Optional<ProductDto> getProduct(String productId) {
        return queryService.findProductById(productId).map(p -> {
            var prices = queryService.findPricesByProductId(productId);
            var priceDtos = prices.stream().map(DtoAssembler::toDto).toList();
            return DtoAssembler.toDto(p, priceDtos);
        });
    }
}
