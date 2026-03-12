package com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.entity.PromotionPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionPolicyJpaRepository extends JpaRepository<PromotionPolicyEntity, UUID> {
    List<PromotionPolicyEntity> findByStatus(String status);
}
