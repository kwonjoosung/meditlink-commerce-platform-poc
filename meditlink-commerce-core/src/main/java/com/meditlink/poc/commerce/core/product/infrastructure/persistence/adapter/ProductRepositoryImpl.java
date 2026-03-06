package com.meditlink.poc.commerce.core.product.infrastructure.persistence.adapter;

import com.meditlink.poc.commerce.core.product.application.port.ProductRepository;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.infrastructure.mapper.ProductMapper;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository.ProductFeatureJpaRepository;
import com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository.ProductJpaRepository;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpa;
    private final ProductFeatureJpaRepository featureJpa;

    public ProductRepositoryImpl(ProductJpaRepository productJpa, ProductFeatureJpaRepository featureJpa) {
        this.productJpa = productJpa;
        this.featureJpa = featureJpa;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        var entity = ProductMapper.toEntity(product);
        var saved = productJpa.save(entity);

        // Features: 기존 삭제 후 재삽입
        featureJpa.deleteByProductId(product.getProductId().value());
        var featureEntities = ProductMapper.toFeatureEntities(product);
        if (!featureEntities.isEmpty()) {
            featureJpa.saveAll(featureEntities);
        }

        var features = featureJpa.findByProductId(saved.getProductId());
        return ProductMapper.toDomain(saved, features);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpa.findById(id.value()).map(entity -> {
            var features = featureJpa.findByProductId(entity.getProductId());
            return ProductMapper.toDomain(entity, features);
        });
    }

    @Override
    public List<Product> findByProductGroupId(ProductGroupId productGroupId) {
        return productJpa.findByProductGroupId(productGroupId.value()).stream()
                .map(entity -> {
                    var features = featureJpa.findByProductId(entity.getProductId());
                    return ProductMapper.toDomain(entity, features);
                })
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(ProductId id) {
        featureJpa.deleteByProductId(id.value());
        productJpa.deleteById(id.value());
    }
}
