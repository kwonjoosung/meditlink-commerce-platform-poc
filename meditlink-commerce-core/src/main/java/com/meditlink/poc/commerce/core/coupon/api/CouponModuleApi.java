package com.meditlink.poc.commerce.core.coupon.api;

import com.meditlink.poc.commerce.core.coupon.api.dto.ApplicableCouponDto;

import java.util.List;
import java.util.UUID;

/**
 * Coupon BC 모듈 API.
 * 다른 BC에서 Coupon BC의 기능을 사용할 때 이 인터페이스를 통해 접근.
 */
public interface CouponModuleApi {

    /**
     * 특정 상품에 적용 가능한 프로모션 목록 조회
     */
    List<ApplicableCouponDto> findApplicablePromotions(UUID productId);
}
