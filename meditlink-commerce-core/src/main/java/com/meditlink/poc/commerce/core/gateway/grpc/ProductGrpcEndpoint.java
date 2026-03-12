package com.meditlink.poc.commerce.core.gateway.grpc;

import com.meditlink.poc.commerce.common.proto.v1.*;
import com.meditlink.poc.commerce.core.product.application.command.ProductCommandService;
import com.meditlink.poc.commerce.core.product.application.dto.CreateProductCommand;
import com.meditlink.poc.commerce.core.product.application.query.ProductQueryService;
import com.meditlink.poc.commerce.core.product.domain.product.ItemType;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroup;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductGrpcEndpoint extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductQueryService queryService;
    private final ProductCommandService commandService;

    public ProductGrpcEndpoint(ProductQueryService queryService, ProductCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<CreateProductResponse> responseObserver) {
        var groups = queryService.findAllProductGroups();
        var activeGroup = groups.stream()
                .filter(pg -> pg.getStatus() == com.meditlink.poc.commerce.core.product.domain.productgroup.ProductGroupStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("활성 ProductGroup이 없습니다"));

        Product product = commandService.create(new CreateProductCommand(
                activeGroup.getProductGroupId().value().toString(),
                request.getName(),
                request.getName(),
                request.getSku(),
                ItemType.SUBSCRIPTION
        ));

        responseObserver.onNext(CreateProductResponse.newBuilder()
                .setProductId(product.getProductId().value().toString())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<GetProductResponse> responseObserver) {
        var productOpt = queryService.findProductById(request.getProductId());

        if (productOpt.isEmpty()) {
            responseObserver.onNext(GetProductResponse.newBuilder()
                    .setFound(false)
                    .build());
            responseObserver.onCompleted();
            return;
        }

        Product product = productOpt.get();
        var prices = queryService.findPricesByProductId(request.getProductId());
        var defaultPrice = prices.stream().filter(p -> p.isDefault()).findFirst();

        responseObserver.onNext(GetProductResponse.newBuilder()
                .setProductId(product.getProductId().value().toString())
                .setSku(product.getDescription() != null ? product.getDescription() : "")
                .setName(product.getName())
                .setBasePrice(defaultPrice.map(p -> (long) p.getAmount()).orElse(0L))
                .setCurrency(defaultPrice.map(p -> p.getCurrency()).orElse("USD"))
                .setFound(true)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void listProductGroups(ListProductGroupsRequest request, StreamObserver<ListProductGroupsResponse> responseObserver) {
        List<ProductGroup> groups = queryService.findAllProductGroups();

        var builder = ListProductGroupsResponse.newBuilder();
        for (ProductGroup pg : groups) {
            builder.addGroups(ProductGroupItem.newBuilder()
                    .setId(pg.getProductGroupId().value().toString())
                    .setCode(pg.getSlug() != null ? pg.getSlug() : "")
                    .setName(pg.getName())
                    .build());
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listProductPlans(ListProductPlansRequest request, StreamObserver<ListProductPlansResponse> responseObserver) {
        var productOpt = queryService.findProductById(request.getProductId());

        if (productOpt.isEmpty()) {
            responseObserver.onNext(ListProductPlansResponse.newBuilder()
                    .setFound(false)
                    .build());
            responseObserver.onCompleted();
            return;
        }

        Product product = productOpt.get();
        var products = queryService.findProductsByProductGroupId(product.getProductGroupId().value().toString());

        var builder = ListProductPlansResponse.newBuilder().setFound(true);
        for (Product p : products) {
            var prices = queryService.findPricesByProductId(p.getProductId().value().toString());
            var defaultPrice = prices.stream().filter(pr -> pr.isDefault()).findFirst();

            builder.addPlans(ProductPlanItem.newBuilder()
                    .setId(p.getProductId().value().toString())
                    .setProductId(p.getProductId().value().toString())
                    .setGroupId(p.getProductGroupId().value().toString())
                    .setPlanCode(p.getItemType().name())
                    .setPlanName(p.getName())
                    .setPrice(defaultPrice.map(pr -> (long) pr.getAmount()).orElse(0L))
                    .setCurrency(defaultPrice.map(pr -> pr.getCurrency()).orElse("USD"))
                    .build());
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
