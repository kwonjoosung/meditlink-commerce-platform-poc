package com.meditlink.poc.commerce.integration.config;

import com.meditlink.poc.commerce.common.proto.v1.CouponServiceGrpc;
import com.meditlink.poc.commerce.common.proto.v1.PriceServiceGrpc;
import com.meditlink.poc.commerce.common.proto.v1.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 학습 포인트: gRPC client 공통 설정
// - 하나의 ManagedChannel을 재사용
// - 서비스별 BlockingStub를 Bean으로 노출해 orchestration에서 사용
@Configuration
public class CoreGrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel coreServiceManagedChannel(
            @Value("${core.grpc.host:localhost}") String host,
            @Value("${core.grpc.port:9090}") int port
    ) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub(ManagedChannel coreServiceManagedChannel) {
        return ProductServiceGrpc.newBlockingStub(coreServiceManagedChannel);
    }

    @Bean
    public PriceServiceGrpc.PriceServiceBlockingStub priceServiceBlockingStub(ManagedChannel coreServiceManagedChannel) {
        return PriceServiceGrpc.newBlockingStub(coreServiceManagedChannel);
    }

    @Bean
    public CouponServiceGrpc.CouponServiceBlockingStub couponServiceBlockingStub(ManagedChannel coreServiceManagedChannel) {
        return CouponServiceGrpc.newBlockingStub(coreServiceManagedChannel);
    }
}
