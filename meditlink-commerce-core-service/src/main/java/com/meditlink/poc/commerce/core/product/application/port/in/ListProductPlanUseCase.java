package com.meditlink.poc.commerce.core.product.application.port.in;

import com.meditlink.poc.commerce.core.product.domain.ProductPlan;
import java.util.List;
import java.util.Optional;

// inbound port: 특정 상품의 플랜 목록 조회
// Optional.empty()는 대상 상품 자체가 없음을 의미
public interface ListProductPlanUseCase {

    Optional<List<ProductPlan>> listPlansByProductId(Long productId);
}
