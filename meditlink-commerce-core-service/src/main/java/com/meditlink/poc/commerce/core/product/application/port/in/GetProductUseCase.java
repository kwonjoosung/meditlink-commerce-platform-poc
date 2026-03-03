package com.meditlink.poc.commerce.core.product.application.port.in;

import com.meditlink.poc.commerce.core.product.domain.Product;
import java.util.Optional;

public interface GetProductUseCase {

    Optional<Product> getById(Long productId);
}
