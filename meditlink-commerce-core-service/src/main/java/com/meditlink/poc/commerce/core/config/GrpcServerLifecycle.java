package com.meditlink.poc.commerce.core;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

// 학습 포인트: gRPC 서버 라이프사이클을 Spring Bean으로 관리
// - SmartLifecycle을 사용하면 애플리케이션 시작/종료와 함께 gRPC 서버를 제어할 수 있음
// - 특정 endpoint 구현체를 직접 의존하지 않고 BindableService 목록을 주입받아
//   Modulith 모듈 경계를 침범하지 않도록 설계
@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private final List<BindableService> bindableServices;
    private final int grpcPort;

    private volatile boolean running;
    private Server server;

    public GrpcServerLifecycle(
            List<BindableService> bindableServices,
            @Value("${grpc.server.port:9090}") int grpcPort
    ) {
        this.bindableServices = bindableServices;
        this.grpcPort = grpcPort;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            NettyServerBuilder builder = NettyServerBuilder.forPort(grpcPort);
            bindableServices.forEach(builder::addService);
            server = builder.build().start();
            running = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start gRPC server", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
