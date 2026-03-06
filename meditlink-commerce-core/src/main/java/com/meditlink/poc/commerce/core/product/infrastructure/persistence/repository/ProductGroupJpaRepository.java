package com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.ProductGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductGroupJpaRepository extends JpaRepository<ProductGroupEntity, UUID> {
    Optional<ProductGroupEntity> findBySlug(String slug);
}
