package com.meditlink.poc.commerce.core.product.adapter.in.grpc;

import com.meditlink.poc.commerce.common.proto.v1.CreateProductRequest;
import com.meditlink.poc.commerce.common.proto.v1.CreateProductResponse;
import com.meditlink.poc.commerce.common.proto.v1.GetProductRequest;
import com.meditlink.poc.commerce.common.proto.v1.GetProductResponse;
import com.meditlink.poc.commerce.common.proto.v1.ListProductGroupsRequest;
import com.meditlink.poc.commerce.common.proto.v1.ListProductGroupsResponse;
import com.meditlink.poc.commerce.common.proto.v1.ListProductPlansRequest;
import com.meditlink.poc.commerce.common.proto.v1.ListProductPlansResponse;
import com.meditlink.poc.commerce.common.proto.v1.ProductGroupItem;
import com.meditlink.poc.commerce.common.proto.v1.ProductPlanItem;
import com.meditlink.poc.commerce.common.proto.v1.ProductServiceGrpc;
import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductCommand;
import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.GetProductUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.ListProductGroupUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.ListProductPlanUseCase;
import com.meditlink.poc.commerce.core.product.domain.Product;
import com.meditlink.poc.commerce.core.product.domain.ProductGroup;
import com.meditlink.poc.commerce.core.product.domain.ProductPlan;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

// Product gRPC Inbound Adapter
// - integration-layer가 호출하는 gRPC Contract의 서버 구현체
@Service
public class ProductGrpcEndpoint extends ProductServiceGrpc.ProductServiceImplBase {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductGroupUseCase listProductGroupUseCase;
    private final ListProductPlanUseCase listProductPlanUseCase;

    public ProductGrpcEndpoint(
            CreateProductUseCase createProductUseCase,
            GetProductUseCase getProductUseCase,
            ListProductGroupUseCase listProductGroupUseCase,
            ListProductPlanUseCase listProductPlanUseCase
    ) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductGroupUseCase = listProductGroupUseCase;
        this.listProductPlanUseCase = listProductPlanUseCase;
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

    @Override
    public void listProductGroups(
            ListProductGroupsRequest request,
            StreamObserver<ListProductGroupsResponse> responseObserver
    ) {
        List<ProductGroup> groups = listProductGroupUseCase.listGroups();

        ListProductGroupsResponse.Builder builder = ListProductGroupsResponse.newBuilder();
        groups.forEach(group -> builder.addGroups(
                ProductGroupItem.newBuilder()
                        .setId(String.valueOf(group.id()))
                        .setCode(group.code())
                        .setName(group.name())
                        .build()
        ));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listProductPlans(
            ListProductPlansRequest request,
            StreamObserver<ListProductPlansResponse> responseObserver
    ) {
        Long productId;
        try {
            productId = Long.parseLong(request.getProductId());
        } catch (NumberFormatException ex) {
            responseObserver.onNext(ListProductPlansResponse.newBuilder().setFound(false).build());
            responseObserver.onCompleted();
            return;
        }

        Optional<List<ProductPlan>> maybePlans = listProductPlanUseCase.listPlansByProductId(productId);
        if (maybePlans.isEmpty()) {
            responseObserver.onNext(ListProductPlansResponse.newBuilder().setFound(false).build());
            responseObserver.onCompleted();
            return;
        }

        ListProductPlansResponse.Builder builder = ListProductPlansResponse.newBuilder().setFound(true);
        maybePlans.get().forEach(plan -> builder.addPlans(
                ProductPlanItem.newBuilder()
                        .setId(String.valueOf(plan.id()))
                        .setProductId(String.valueOf(plan.productId()))
                        .setGroupId(String.valueOf(plan.groupId()))
                        .setPlanCode(plan.planCode())
                        .setPlanName(plan.planName())
                        .setPrice(plan.price())
                        .setCurrency(plan.currency())
                        .build()
        ));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
