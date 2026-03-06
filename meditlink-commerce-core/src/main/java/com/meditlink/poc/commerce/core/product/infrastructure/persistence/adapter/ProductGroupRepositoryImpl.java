package com.meditlink.poc.commerce.core.product.infrastructure.persistence.adapter;

import com.meditlink.poc.commerce.core.product.application.port.ProductGroupRepository;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import com.meditlink.poc.commerce.core.product.infrastructure.mapper.ProductGroupMapper;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository.ProductGroupJpaRepository;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductGroupRepositoryImpl implements ProductGroupRepository {

    private final ProductGroupJpaRepository jpa;

    public ProductGroupRepositoryImpl(ProductGroupJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ProductGroup save(ProductGroup productGroup) {
        var entity = ProductGroupMapper.toEntity(productGroup);
        var saved = jpa.save(entity);
        return ProductGroupMapper.toDomain(saved);
    }

    @Override
    public Optional<ProductGroup> findById(ProductGroupId id) {
        return jpa.findById(id.value()).map(ProductGroupMapper::toDomain);
    }

    @Override
    public Optional<ProductGroup> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(ProductGroupMapper::toDomain);
    }

    @Override
    public List<ProductGroup> findAll() {
        return jpa.findAll().stream().map(ProductGroupMapper::toDomain).toList();
    }

    @Override
    public void deleteById(ProductGroupId id) {
        jpa.deleteById(id.value());
    }
}
