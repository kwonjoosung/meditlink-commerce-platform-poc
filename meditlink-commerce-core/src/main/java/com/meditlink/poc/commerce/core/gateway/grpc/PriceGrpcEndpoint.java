package com.meditlink.poc.commerce.core.gateway.grpc;

import com.meditlink.poc.commerce.common.proto.v1.CalculatePriceRequest;
import com.meditlink.poc.commerce.common.proto.v1.CalculatePriceResponse;
import com.meditlink.poc.commerce.common.proto.v1.PriceServiceGrpc;
import com.meditlink.poc.commerce.core.product.application.query.ProductQueryService;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

/**
 * Price gRPC Inbound Adapter.
 * Client(integration-layer)에서 호출하는 PriceService gRPC 엔드포인트 구현.
 */
@Service
public class PriceGrpcEndpoint extends PriceServiceGrpc.PriceServiceImplBase {

    private final ProductQueryService queryService;

    public PriceGrpcEndpoint(ProductQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public void calculatePrice(CalculatePriceRequest request, StreamObserver<CalculatePriceResponse> responseObserver) {
        var productOpt = queryService.findProductById(request.getProductId());

        if (productOpt.isEmpty()) {
            responseObserver.onNext(CalculatePriceResponse.newBuilder()
                    .setFound(false)
                    .build());
            responseObserver.onCompleted();
            return;
        }

        // 기본 가격 조회
        var prices = queryService.findPricesByProductId(request.getProductId());
        var defaultPrice = prices.stream().filter(p -> p.isDefault()).findFirst();

        long basePrice = defaultPrice.map(p -> (long) p.getAmount()).orElse(0L);
        String currency = defaultPrice.map(p -> p.getCurrency()).orElse("USD");

        // 쿠폰 적용 (간소화: 쿠폰코드가 있으면 10% 할인)
        long finalPrice = basePrice;
        String appliedCoupon = "";
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            finalPrice = basePrice * 90 / 100;
            appliedCoupon = request.getCouponCode();
        }

        responseObserver.onNext(CalculatePriceResponse.newBuilder()
                .setProductId(request.getProductId())
                .setFinalPrice(finalPrice)
                .setCurrency(currency)
                .setAppliedCouponCode(appliedCoupon)
                .setFound(true)
                .build());
        responseObserver.onCompleted();
    }
}
