package com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findByProductGroupId(UUID productGroupId);
    List<ProductEntity> findByProductGroupIdAndStatus(UUID productGroupId, String status);
}
