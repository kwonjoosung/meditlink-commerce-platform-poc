package com.meditlink.poc.commerce.integration.admin.web;

import com.meditlink.poc.commerce.integration.orchestration.ProductOrchestrationService;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductHttpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductOrchestrationService productOrchestrationService;

    public AdminProductController(ProductOrchestrationService productOrchestrationService) {
        this.productOrchestrationService = productOrchestrationService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductHttpResponse> find(@PathVariable String productId) {
        ProductHttpResponse response = productOrchestrationService.getProduct(productId);
        if (!response.found()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
