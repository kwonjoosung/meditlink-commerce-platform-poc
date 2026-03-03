package com.meditlink.poc.commerce.core;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

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
