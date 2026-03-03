package com.meditlink.poc.commerce.core.coupon.adapter.in.grpc;

import com.meditlink.poc.commerce.common.proto.v1.CouponServiceGrpc;
import com.meditlink.poc.commerce.common.proto.v1.IssueCouponRequest;
import com.meditlink.poc.commerce.common.proto.v1.IssueCouponResponse;
import com.meditlink.poc.commerce.core.coupon.application.port.in.IssueCouponCommand;
import com.meditlink.poc.commerce.core.coupon.application.port.in.IssueCouponUseCase;
import com.meditlink.poc.commerce.core.coupon.domain.Coupon;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import org.springframework.stereotype.Service;

// Coupon gRPC Inbound Adapter
@Service
public class CouponGrpcEndpoint extends CouponServiceGrpc.CouponServiceImplBase {

    private final IssueCouponUseCase issueCouponUseCase;

    public CouponGrpcEndpoint(IssueCouponUseCase issueCouponUseCase) {
        this.issueCouponUseCase = issueCouponUseCase;
    }

    @Override
    public void issueCoupon(IssueCouponRequest request, StreamObserver<IssueCouponResponse> responseObserver) {
        Instant expiresAt = request.getExpiresAtEpochMillis() <= 0
                ? Instant.now().plusSeconds(30L * 24 * 60 * 60)
                : Instant.ofEpochMilli(request.getExpiresAtEpochMillis());

        Coupon issued = issueCouponUseCase.issue(new IssueCouponCommand(
                request.getCode(),
                request.getDiscountRate(),
                expiresAt
        ));

        IssueCouponResponse response = IssueCouponResponse.newBuilder()
                .setIssued(true)
                .setCode(issued.code())
                .setDiscountRate(issued.discountRate())
                .setExpiresAtEpochMillis(issued.expiresAt().toEpochMilli())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
