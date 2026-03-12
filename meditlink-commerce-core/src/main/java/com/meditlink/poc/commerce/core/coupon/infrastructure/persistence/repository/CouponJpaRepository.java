package com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.repository;

import com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, UUID> {
    Optional<CouponEntity> findByCode(String code);
    List<CouponEntity> findByCustomerId(String customerId);
    List<CouponEntity> findByPolicyId(UUID policyId);
}
