package com.meditlink.poc.commerce.core.price.adapter.in.grpc;

import com.meditlink.poc.commerce.common.proto.v1.CalculatePriceRequest;
import com.meditlink.poc.commerce.common.proto.v1.CalculatePriceResponse;
import com.meditlink.poc.commerce.common.proto.v1.PriceServiceGrpc;
import com.meditlink.poc.commerce.core.price.application.port.in.CalculatePriceUseCase;
import com.meditlink.poc.commerce.core.price.domain.PriceQuote;
import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

// Price gRPC Inbound Adapter
@Service
public class PriceGrpcEndpoint extends PriceServiceGrpc.PriceServiceImplBase {

    private final CalculatePriceUseCase calculatePriceUseCase;

    public PriceGrpcEndpoint(CalculatePriceUseCase calculatePriceUseCase) {
        this.calculatePriceUseCase = calculatePriceUseCase;
    }

    @Override
    public void calculatePrice(CalculatePriceRequest request, StreamObserver<CalculatePriceResponse> responseObserver) {
        Long productId;
        try {
            productId = Long.parseLong(request.getProductId());
        } catch (NumberFormatException ex) {
            responseObserver.onNext(CalculatePriceResponse.newBuilder().setFound(false).build());
            responseObserver.onCompleted();
            return;
        }

        try {
            PriceQuote quote = calculatePriceUseCase.calculate(productId, request.getCouponCode());
            CalculatePriceResponse response = CalculatePriceResponse.newBuilder()
                    .setFound(true)
                    .setProductId(String.valueOf(quote.productId()))
                    .setFinalPrice(quote.finalPrice())
                    .setCurrency(quote.currency())
                    .setAppliedCouponCode(quote.appliedCouponCode() == null ? "" : quote.appliedCouponCode())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (EntityNotFoundException ex) {
            responseObserver.onNext(CalculatePriceResponse.newBuilder().setFound(false).build());
            responseObserver.onCompleted();
        }
    }
}
