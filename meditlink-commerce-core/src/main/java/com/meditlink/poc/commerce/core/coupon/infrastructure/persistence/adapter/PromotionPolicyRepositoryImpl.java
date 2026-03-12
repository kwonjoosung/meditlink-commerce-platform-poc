package com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.adapter;

import com.meditlink.poc.commerce.core.coupon.application.port.PromotionPolicyRepository;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionStatus;
import com.meditlink.poc.commerce.core.coupon.infrastructure.mapper.PromotionPolicyMapper;
import com.meditlink.poc.commerce.core.coupon.infrastructure.persistence.repository.PromotionPolicyJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PromotionPolicyRepositoryImpl implements PromotionPolicyRepository {

    private final PromotionPolicyJpaRepository jpa;

    public PromotionPolicyRepositoryImpl(PromotionPolicyJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PromotionPolicy save(PromotionPolicy policy) {
        var entity = PromotionPolicyMapper.toEntity(policy);
        var saved = jpa.save(entity);
        return PromotionPolicyMapper.toDomain(saved);
    }

    @Override
    public Optional<PromotionPolicy> findById(UUID policyId) {
        return jpa.findById(policyId).map(PromotionPolicyMapper::toDomain);
    }

    @Override
    public List<PromotionPolicy> findAllActive() {
        return jpa.findByStatus(PromotionStatus.ACTIVE.name()).stream()
                .map(PromotionPolicyMapper::toDomain).toList();
    }
}
