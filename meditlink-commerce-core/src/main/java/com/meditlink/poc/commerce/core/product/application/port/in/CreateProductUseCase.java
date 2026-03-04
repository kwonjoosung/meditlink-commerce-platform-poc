package com.meditlink.poc.commerce.core.product.application.port.in;

import com.meditlink.poc.commerce.core.product.domain.Product;

// inbound port: 상품 생성 유스케이스
public interface CreateProductUseCase {

    Product create(CreateProductCommand command);
}
