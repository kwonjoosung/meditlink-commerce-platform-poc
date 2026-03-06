package com.meditlink.poc.commerce.core.product.api;

import com.meditlink.poc.commerce.core.product.api.dto.ProductDto;
import com.meditlink.poc.commerce.core.product.api.dto.ProductFeatureDto;
import com.meditlink.poc.commerce.core.product.api.dto.ProductGroupDto;

import java.util.List;
import java.util.Optional;

/**
 * Product 모듈의 Public API.
 * 다른 BC 모듈이 Java 인터페이스로 호출한다.
 */
public interface ProductModuleApi {

    List<ProductGroupDto> getActiveProductGroups();

    List<ProductFeatureDto> getProductFeatures(String productId);

    Optional<ProductDto> getProduct(String productId);
}
