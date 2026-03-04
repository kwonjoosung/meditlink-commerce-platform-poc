package com.meditlink.poc.commerce.integration.web;

import com.meditlink.poc.commerce.integration.gateway.grpc.CoreProductGrpcGateway;
import com.meditlink.poc.commerce.integration.orchestration.ProductOrchestrationService;
import com.meditlink.poc.commerce.integration.orchestration.dto.CreateProductHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.PriceQuoteHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductGroupHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductPlanHttpResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// BFF Adapter
// - 클라이언트 친화적인 REST API 제공
// - 내부적으로 orchestration + gRPC 호출 흐름을 숨김
@RestController
@RequestMapping("/api/bff")
public class ProductBffController {

    private final ProductOrchestrationService productOrchestrationService;

    public ProductBffController(ProductOrchestrationService productOrchestrationService) {
        this.productOrchestrationService = productOrchestrationService;
    }

    @PostMapping("/products")
    public ResponseEntity<CreateProductBffResponse> create(@Valid @RequestBody CreateProductHttpRequest request) {
        String productId = productOrchestrationService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateProductBffResponse(productId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductHttpResponse> getById(@PathVariable String productId) {
        ProductHttpResponse response = productOrchestrationService.getProduct(productId);
        if (!response.found()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}/price")
    public ResponseEntity<PriceQuoteHttpResponse> calculatePrice(
            @PathVariable String productId,
            @RequestParam(required = false) String couponCode
    ) {
        PriceQuoteHttpResponse response = productOrchestrationService.calculatePrice(productId, couponCode);
        if (!response.found()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/coupons")
    public ResponseEntity<IssueCouponHttpResponse> issueCoupon(@Valid @RequestBody IssueCouponHttpRequest request) {
        IssueCouponHttpResponse response = productOrchestrationService.issueCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product-groups")
    public ResponseEntity<List<ProductGroupHttpResponse>> listProductGroups() {
        return ResponseEntity.ok(productOrchestrationService.listProductGroups());
    }

    @GetMapping("/products/{productId}/plans")
    public ResponseEntity<List<ProductPlanHttpResponse>> listProductPlans(@PathVariable String productId) {
        CoreProductGrpcGateway.ProductPlansResult result = productOrchestrationService.listProductPlans(productId);
        if (!result.found()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result.plans());
    }

    public record CreateProductBffResponse(String productId) {
    }
}
