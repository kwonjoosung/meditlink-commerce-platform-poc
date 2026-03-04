package com.meditlink.poc.commerce.integration.orchestration;

import com.meditlink.poc.commerce.integration.gateway.grpc.CoreProductGrpcGateway;
import com.meditlink.poc.commerce.integration.orchestration.dto.CreateProductHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.PriceQuoteHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductGroupHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductPlanHttpResponse;
import com.meditlink.poc.commerce.integration.state.RequestLogService;
import java.util.List;
import org.springframework.stereotype.Service;

// orchestration 계층
// - BFF 요청 흐름을 묶고, gateway 호출 + 상태 로그를 관리
@Service
public class ProductOrchestrationService {

    private final CoreProductGrpcGateway coreProductGrpcGateway;
    private final RequestLogService requestLogService;

    public ProductOrchestrationService(CoreProductGrpcGateway coreProductGrpcGateway, RequestLogService requestLogService) {
        this.coreProductGrpcGateway = coreProductGrpcGateway;
        this.requestLogService = requestLogService;
    }

    public String createProduct(CreateProductHttpRequest request) {
        String productId = coreProductGrpcGateway.createProduct(request);
        requestLogService.log("CREATE_PRODUCT", productId, "SUCCESS", "{\"sku\":\"" + request.sku() + "\"}");
        return productId;
    }

    public ProductHttpResponse getProduct(String productId) {
        ProductHttpResponse response = coreProductGrpcGateway.getProduct(productId);
        requestLogService.log("GET_PRODUCT", productId, response.found() ? "SUCCESS" : "NOT_FOUND", null);
        return response;
    }

    public PriceQuoteHttpResponse calculatePrice(String productId, String couponCode) {
        PriceQuoteHttpResponse response = coreProductGrpcGateway.calculatePrice(productId, couponCode);
        requestLogService.log("CALCULATE_PRICE", productId, response.found() ? "SUCCESS" : "NOT_FOUND", null);
        return response;
    }

    public IssueCouponHttpResponse issueCoupon(IssueCouponHttpRequest request) {
        IssueCouponHttpResponse response = coreProductGrpcGateway.issueCoupon(request);
        requestLogService.log("ISSUE_COUPON", response.code(), response.issued() ? "SUCCESS" : "FAILED", null);
        return response;
    }

    public List<ProductGroupHttpResponse> listProductGroups() {
        List<ProductGroupHttpResponse> response = coreProductGrpcGateway.listProductGroups();
        requestLogService.log("LIST_PRODUCT_GROUPS", null, "SUCCESS", null);
        return response;
    }

    public CoreProductGrpcGateway.ProductPlansResult listProductPlans(String productId) {
        CoreProductGrpcGateway.ProductPlansResult response = coreProductGrpcGateway.listProductPlans(productId);
        requestLogService.log("LIST_PRODUCT_PLANS", productId, response.found() ? "SUCCESS" : "NOT_FOUND", null);
        return response;
    }

    public record ProductPlansResult(boolean found, List<ProductPlanHttpResponse> plans) {
    }
}
