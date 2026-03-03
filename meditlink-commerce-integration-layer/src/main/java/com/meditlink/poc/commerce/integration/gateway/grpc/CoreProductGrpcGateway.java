package com.meditlink.poc.commerce.integration.gateway.grpc;

import com.meditlink.poc.commerce.common.proto.v1.CalculatePriceRequest;
import com.meditlink.poc.commerce.common.proto.v1.CalculatePriceResponse;
import com.meditlink.poc.commerce.common.proto.v1.CreateProductRequest;
import com.meditlink.poc.commerce.common.proto.v1.CreateProductResponse;
import com.meditlink.poc.commerce.common.proto.v1.GetProductRequest;
import com.meditlink.poc.commerce.common.proto.v1.GetProductResponse;
import com.meditlink.poc.commerce.common.proto.v1.IssueCouponRequest;
import com.meditlink.poc.commerce.common.proto.v1.IssueCouponResponse;
import com.meditlink.poc.commerce.common.proto.v1.ListProductGroupsRequest;
import com.meditlink.poc.commerce.common.proto.v1.ListProductPlansRequest;
import com.meditlink.poc.commerce.common.proto.v1.CouponServiceGrpc;
import com.meditlink.poc.commerce.common.proto.v1.PriceServiceGrpc;
import com.meditlink.poc.commerce.common.proto.v1.ProductServiceGrpc;
import com.meditlink.poc.commerce.integration.orchestration.dto.CreateProductHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpRequest;
import com.meditlink.poc.commerce.integration.orchestration.dto.IssueCouponHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.PriceQuoteHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductGroupHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductHttpResponse;
import com.meditlink.poc.commerce.integration.orchestration.dto.ProductPlanHttpResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

// gRPC Outbound Adapter
// - integration-layer의 orchestration 로직이 core-service gRPC를 호출하는 단일 진입점
@Component
public class CoreProductGrpcGateway {

    private final ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub;
    private final PriceServiceGrpc.PriceServiceBlockingStub priceServiceBlockingStub;
    private final CouponServiceGrpc.CouponServiceBlockingStub couponServiceBlockingStub;

    public CoreProductGrpcGateway(
            ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub,
            PriceServiceGrpc.PriceServiceBlockingStub priceServiceBlockingStub,
            CouponServiceGrpc.CouponServiceBlockingStub couponServiceBlockingStub
    ) {
        this.productServiceBlockingStub = productServiceBlockingStub;
        this.priceServiceBlockingStub = priceServiceBlockingStub;
        this.couponServiceBlockingStub = couponServiceBlockingStub;
    }

    public String createProduct(CreateProductHttpRequest request) {
        CreateProductResponse response = productServiceBlockingStub.createProduct(
                CreateProductRequest.newBuilder()
                        .setSku(request.sku())
                        .setName(request.name())
                        .setBasePrice(request.basePrice())
                        .setCurrency(request.currency())
                        .build()
        );
        return response.getProductId();
    }

    public ProductHttpResponse getProduct(String productId) {
        GetProductResponse response = productServiceBlockingStub.getProduct(
                GetProductRequest.newBuilder()
                        .setProductId(productId)
                        .build()
        );

        return new ProductHttpResponse(
                response.getProductId(),
                response.getSku(),
                response.getName(),
                response.getBasePrice(),
                response.getCurrency(),
                response.getFound()
        );
    }

    public PriceQuoteHttpResponse calculatePrice(String productId, String couponCode) {
        CalculatePriceResponse response = priceServiceBlockingStub.calculatePrice(
                CalculatePriceRequest.newBuilder()
                        .setProductId(productId)
                        .setCouponCode(couponCode == null ? "" : couponCode)
                        .build()
        );

        return new PriceQuoteHttpResponse(
                response.getProductId(),
                response.getFinalPrice(),
                response.getCurrency(),
                response.getAppliedCouponCode(),
                response.getFound()
        );
    }

    public IssueCouponHttpResponse issueCoupon(IssueCouponHttpRequest request) {
        long expiresAtEpochMillis = request.expiresAt() == null ? 0L : request.expiresAt().toEpochMilli();
        IssueCouponResponse response = couponServiceBlockingStub.issueCoupon(
                IssueCouponRequest.newBuilder()
                        .setCode(request.code())
                        .setDiscountRate(request.discountRate())
                        .setExpiresAtEpochMillis(expiresAtEpochMillis)
                        .build()
        );

        return new IssueCouponHttpResponse(
                response.getCode(),
                response.getDiscountRate(),
                Instant.ofEpochMilli(response.getExpiresAtEpochMillis()),
                response.getIssued()
        );
    }

    public List<ProductGroupHttpResponse> listProductGroups() {
        return productServiceBlockingStub
                .listProductGroups(ListProductGroupsRequest.newBuilder().build())
                .getGroupsList()
                .stream()
                .map(group -> new ProductGroupHttpResponse(
                        group.getId(),
                        group.getCode(),
                        group.getName()
                ))
                .toList();
    }

    public ProductPlansResult listProductPlans(String productId) {
        var response = productServiceBlockingStub.listProductPlans(
                ListProductPlansRequest.newBuilder()
                        .setProductId(productId)
                        .build()
        );

        List<ProductPlanHttpResponse> plans = response.getPlansList()
                .stream()
                .map(plan -> new ProductPlanHttpResponse(
                        plan.getId(),
                        plan.getProductId(),
                        plan.getGroupId(),
                        plan.getPlanCode(),
                        plan.getPlanName(),
                        plan.getPrice(),
                        plan.getCurrency()
                ))
                .toList();

        return new ProductPlansResult(response.getFound(), plans);
    }

    public record ProductPlansResult(boolean found, List<ProductPlanHttpResponse> plans) {
    }
}
