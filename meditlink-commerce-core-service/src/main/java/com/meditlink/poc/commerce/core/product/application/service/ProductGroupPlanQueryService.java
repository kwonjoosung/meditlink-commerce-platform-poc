package com.meditlink.poc.commerce.core.product.application.service;

import com.meditlink.poc.commerce.core.product.application.port.in.ListProductGroupUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.ListProductPlanUseCase;
import com.meditlink.poc.commerce.core.product.application.port.out.LoadProductGroupPort;
import com.meditlink.poc.commerce.core.product.application.port.out.LoadProductPlanPort;
import com.meditlink.poc.commerce.core.product.application.port.out.LoadProductPort;
import com.meditlink.poc.commerce.core.product.domain.ProductPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// group/plan 조회 전용 서비스
// - Product 존재 검증 후 plan 목록 반환
@Service
@Transactional(readOnly = true)
public class ProductGroupPlanQueryService implements ListProductGroupUseCase, ListProductPlanUseCase {

    private final LoadProductPort loadProductPort;
    private final LoadProductGroupPort loadProductGroupPort;
    private final LoadProductPlanPort loadProductPlanPort;

    public ProductGroupPlanQueryService(
            LoadProductPort loadProductPort,
            LoadProductGroupPort loadProductGroupPort,
            LoadProductPlanPort loadProductPlanPort
    ) {
        this.loadProductPort = loadProductPort;
        this.loadProductGroupPort = loadProductGroupPort;
        this.loadProductPlanPort = loadProductPlanPort;
    }

    @Override
    public java.util.List<com.meditlink.poc.commerce.core.product.domain.ProductGroup> listGroups() {
        return loadProductGroupPort.loadAllGroups();
    }

    @Override
    public Optional<List<ProductPlan>> listPlansByProductId(Long productId) {
        if (loadProductPort.loadById(productId).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(loadProductPlanPort.loadByProductId(productId));
    }
}
