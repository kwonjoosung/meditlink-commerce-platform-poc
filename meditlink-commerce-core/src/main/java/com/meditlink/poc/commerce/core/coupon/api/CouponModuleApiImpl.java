package com.meditlink.poc.commerce.core.coupon.api;

import com.meditlink.poc.commerce.core.coupon.api.dto.ApplicableCouponDto;
import com.meditlink.poc.commerce.core.coupon.application.command.PromotionPolicyService;
import com.meditlink.poc.commerce.core.coupon.domain.PromotionPolicy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CouponModuleApiImpl implements CouponModuleApi {

    private final PromotionPolicyService promotionPolicyService;

    public CouponModuleApiImpl(PromotionPolicyService promotionPolicyService) {
        this.promotionPolicyService = promotionPolicyService;
    }

    @Override
    public List<ApplicableCouponDto> findApplicablePromotions(UUID productId) {
        return promotionPolicyService.findApplicablePolicies(productId).stream()
                .map(this::toDto)
                .toList();
    }

    private ApplicableCouponDto toDto(PromotionPolicy policy) {
        return new ApplicableCouponDto(
                policy.getPolicyId(),
                policy.getName(),
                policy.getDescription(),
                policy.getDiscountType(),
                policy.getDiscountValue()
        );
    }
}
