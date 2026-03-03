package com.meditlink.poc.commerce.core.product.adapter.in.grpc;

import com.meditlink.poc.commerce.common.proto.v1.CreateProductRequest;
import com.meditlink.poc.commerce.common.proto.v1.CreateProductResponse;
import com.meditlink.poc.commerce.common.proto.v1.GetProductRequest;
import com.meditlink.poc.commerce.common.proto.v1.GetProductResponse;
import com.meditlink.poc.commerce.common.proto.v1.ProductServiceGrpc;
import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductCommand;
import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.GetProductUseCase;
import com.meditlink.poc.commerce.core.product.domain.Product;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductGrpcEndpoint extends ProductServiceGrpc.ProductServiceImplBase {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;

    public ProductGrpcEndpoint(CreateProductUseCase createProductUseCase, GetProductUseCase getProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<CreateProductResponse> responseObserver) {
        Product created = createProductUseCase.create(new CreateProductCommand(
                request.getSku(),
                request.getName(),
                request.getBasePrice(),
                request.getCurrency()
        ));

        CreateProductResponse response = CreateProductResponse.newBuilder()
                .setProductId(String.valueOf(created.id()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<GetProductResponse> responseObserver) {
        Long productId;
        try {
            productId = Long.parseLong(request.getProductId());
        } catch (NumberFormatException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("product_id must be numeric")
                    .asRuntimeException());
            return;
        }

        Optional<Product> maybeProduct = getProductUseCase.getById(productId);

        if (maybeProduct.isEmpty()) {
            responseObserver.onNext(GetProductResponse.newBuilder().setFound(false).build());
            responseObserver.onCompleted();
            return;
        }

        Product product = maybeProduct.get();
        GetProductResponse response = GetProductResponse.newBuilder()
                .setFound(true)
                .setProductId(String.valueOf(product.id()))
                .setSku(product.sku())
                .setName(product.name())
                .setBasePrice(product.basePrice())
                .setCurrency(product.currency())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
