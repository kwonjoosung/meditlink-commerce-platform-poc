package com.meditlink.poc.commerce.core.product.application.port.in;

import com.meditlink.poc.commerce.core.product.domain.Product;
import java.util.Optional;

// inbound port: 상품 단건 조회 유스케이스
public interface GetProductUseCase {

    Optional<Product> getById(Long productId);
}
