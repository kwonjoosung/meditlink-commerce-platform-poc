package com.meditlink.poc.commerce.core.product.application.port;

import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;

import java.util.List;
import java.util.Optional;

public interface PriceRepository {
    Price save(Price price);
    Optional<Price> findById(PriceId id);
    List<Price> findByProductId(ProductId productId);
    void deleteById(PriceId id);
}
