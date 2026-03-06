package com.meditlink.poc.commerce.core.product.application.port;

import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    List<Product> findByProductGroupId(ProductGroupId productGroupId);
    void deleteById(ProductId id);
}
