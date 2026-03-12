package com.meditlink.poc.commerce.core.coupon.application.port;

import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionPolicyRepository {
    PromotionPolicy save(PromotionPolicy policy);
    Optional<PromotionPolicy> findById(UUID policyId);
    List<PromotionPolicy> findAllActive();
}
