package com.meditlink.poc.commerce.core.product.application.port.in;

import com.meditlink.poc.commerce.core.product.domain.Product;

public interface CreateProductUseCase {

    Product create(CreateProductCommand command);
}
