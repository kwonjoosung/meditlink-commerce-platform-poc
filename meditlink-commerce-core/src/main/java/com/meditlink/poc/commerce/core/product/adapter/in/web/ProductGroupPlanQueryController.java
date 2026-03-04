package com.meditlink.poc.commerce.core.product.adapter.in.web;

import com.meditlink.poc.commerce.core.product.application.port.in.ListProductGroupUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.ListProductPlanUseCase;
import com.meditlink.poc.commerce.core.product.domain.ProductGroup;
import com.meditlink.poc.commerce.core.product.domain.ProductPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Group/Plan 조회용 HTTP Inbound Adapter
@RestController
@RequestMapping("/api/products")
public class ProductGroupPlanQueryController {

    private final ListProductGroupUseCase listProductGroupUseCase;
    private final ListProductPlanUseCase listProductPlanUseCase;

    public ProductGroupPlanQueryController(
            ListProductGroupUseCase listProductGroupUseCase,
            ListProductPlanUseCase listProductPlanUseCase
    ) {
        this.listProductGroupUseCase = listProductGroupUseCase;
        this.listProductPlanUseCase = listProductPlanUseCase;
    }

    @GetMapping("/groups")
    public List<ProductGroup> listGroups() {
        return listProductGroupUseCase.listGroups();
    }

    @GetMapping("/{productId}/plans")
    public ResponseEntity<List<ProductPlan>> listPlans(@PathVariable Long productId) {
        Optional<List<ProductPlan>> plans = listProductPlanUseCase.listPlansByProductId(productId);
        return plans.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
