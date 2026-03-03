package com.meditlink.poc.commerce.integration.orchestration;

import com.meditlink.poc.commerce.integration.gateway.grpc.CoreProductGrpcGateway;
import com.meditlink.poc.commerce.integration.orchestration.dto.CreateProductHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.PriceQuoteHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductHttpResponse;
import com.meditlink.poc.commerce.integration.state.RequestLogService;
import org.springframework.stereotype.Service;

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
}
