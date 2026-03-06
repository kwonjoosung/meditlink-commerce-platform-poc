package com.meditlink.poc.commerce.core.product.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.product.infrastructure.persistence.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PriceJpaRepository extends JpaRepository<PriceEntity, UUID> {
    List<PriceEntity> findByProductId(UUID productId);
    List<PriceEntity> findByProductIdAndCurrency(UUID productId, String currency);
}
