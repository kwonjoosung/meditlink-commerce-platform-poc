package com.meditlink.poc.commerce.core.product.application.port;

import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;

import java.util.List;
import java.util.Optional;

public interface ProductGroupRepository {
    ProductGroup save(ProductGroup productGroup);
    Optional<ProductGroup> findById(ProductGroupId id);
    Optional<ProductGroup> findBySlug(String slug);
    List<ProductGroup> findAll();
    void deleteById(ProductGroupId id);
}
