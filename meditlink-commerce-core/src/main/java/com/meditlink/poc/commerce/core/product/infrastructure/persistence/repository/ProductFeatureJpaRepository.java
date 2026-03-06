package com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductFeatureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductFeatureJpaRepository extends JpaRepository<ProductFeatureEntity, ProductFeatureEntity.ProductFeatureId> {
    List<ProductFeatureEntity> findByProductId(UUID productId);
    void deleteByProductId(UUID productId);
}
