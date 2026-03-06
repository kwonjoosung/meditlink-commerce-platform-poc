package com.meditlink.poc.commerce.core.gateway;

import com.meditlink.poc.commerce.core.product.api.dto.*;
import com.meditlink.poc.commerce.core.product.application.command.PriceCommandService;
import com.meditlink.poc.commerce.core.product.application.command.ProductCommandService;
import com.meditlink.poc.commerce.core.product.application.command.ProductGroupCommandService;
import com.meditlink.poc.commerce.core.product.application.dto.*;
import com.meditlink.poc.commerce.core.product.application.query.ProductQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductGroupCommandService productGroupCommandService;
    private final ProductCommandService productCommandService;
    private final PriceCommandService priceCommandService;
    private final ProductQueryService queryService;

    public ProductController(ProductGroupCommandService productGroupCommandService,
                             ProductCommandService productCommandService,
                             PriceCommandService priceCommandService,
                             ProductQueryService queryService) {
        this.productGroupCommandService = productGroupCommandService;
        this.productCommandService = productCommandService;
        this.priceCommandService = priceCommandService;
        this.queryService = queryService;
    }

    // ── ProductGroup CRUD ──

    @PostMapping("/product-groups")
    public ResponseEntity<ProductGroupDto> createProductGroup(@RequestBody CreateProductGroupCommand cmd) {
        var pg = productGroupCommandService.create(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(pg));
    }

    @GetMapping("/product-groups")
    public List<ProductGroupDto> listProductGroups() {
        return queryService.findAllProductGroups().stream()
                .map(pg -> {
                    var products = queryService.findProductsByProductGroupId(pg.getProductGroupId().toString());
                    var productDtos = products.stream().map(p -> {
                        var prices = queryService.findPricesByProductId(p.getProductId().toString());
                        return DtoAssembler.toDto(p, prices.stream().map(DtoAssembler::toDto).toList());
                    }).toList();
                    return DtoAssembler.toDto(pg, productDtos);
                })
                .toList();
    }

    @GetMapping("/product-groups/{id}")
    public ResponseEntity<ProductGroupDto> getProductGroup(@PathVariable String id) {
        return queryService.findProductGroupById(id)
                .map(pg -> {
                    var products = queryService.findProductsByProductGroupId(id);
                    var productDtos = products.stream().map(p -> {
                        var prices = queryService.findPricesByProductId(p.getProductId().toString());
                        return DtoAssembler.toDto(p, prices.stream().map(DtoAssembler::toDto).toList());
                    }).toList();
                    return ResponseEntity.ok(DtoAssembler.toDto(pg, productDtos));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/product-groups/{id}")
    public ResponseEntity<ProductGroupDto> updateProductGroup(@PathVariable String id,
                                                              @RequestBody UpdateProductGroupCommand cmd) {
        var pg = productGroupCommandService.update(id, cmd);
        return ResponseEntity.ok(DtoAssembler.toDto(pg));
    }

    @DeleteMapping("/product-groups/{id}")
    public ResponseEntity<Void> deleteProductGroup(@PathVariable String id) {
        productGroupCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product-groups/{id}/activate")
    public ResponseEntity<ProductGroupDto> activateProductGroup(@PathVariable String id) {
        var pg = productGroupCommandService.activate(id);
        return ResponseEntity.ok(DtoAssembler.toDto(pg));
    }

    @PostMapping("/product-groups/{id}/archive")
    public ResponseEntity<ProductGroupDto> archiveProductGroup(@PathVariable String id) {
        var pg = productGroupCommandService.archive(id);
        return ResponseEntity.ok(DtoAssembler.toDto(pg));
    }

    // ── Product CRUD ──

    @PostMapping("/product-groups/{productGroupId}/products")
    public ResponseEntity<ProductDto> createProduct(@PathVariable String productGroupId,
                                                    @RequestBody CreateProductCommand cmd) {
        var fullCmd = new CreateProductCommand(
                productGroupId, cmd.name(), cmd.description(), cmd.type(), cmd.billingType(),
                cmd.condition(), cmd.attributes(), cmd.metadata(), cmd.tags()
        );
        var product = productCommandService.create(fullCmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(product));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String id) {
        return queryService.findProductById(id)
                .map(p -> {
                    var prices = queryService.findPricesByProductId(id);
                    return ResponseEntity.ok(DtoAssembler.toDto(p, prices.stream().map(DtoAssembler::toDto).toList()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id,
                                                    @RequestBody UpdateProductCommand cmd) {
        var product = productCommandService.update(id, cmd);
        return ResponseEntity.ok(DtoAssembler.toDto(product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── ProductFeature ──

    @PostMapping("/products/{productId}/features")
    public ResponseEntity<ProductDto> addFeature(@PathVariable String productId,
                                                 @RequestBody AddFeatureCommand cmd) {
        var product = productCommandService.addFeature(productId, cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(product));
    }

    @DeleteMapping("/products/{productId}/features/{featureCode}")
    public ResponseEntity<ProductDto> removeFeature(@PathVariable String productId,
                                                    @PathVariable String featureCode) {
        var product = productCommandService.removeFeature(productId, featureCode);
        return ResponseEntity.ok(DtoAssembler.toDto(product));
    }

    @GetMapping("/products/{productId}/features")
    public List<ProductFeatureDto> listFeatures(@PathVariable String productId) {
        return queryService.findProductById(productId)
                .map(p -> p.getFeatures().stream().map(DtoAssembler::toDto).toList())
                .orElse(List.of());
    }

    // ── Price CRUD ──

    @PostMapping("/products/{productId}/prices")
    public ResponseEntity<PriceDto> createPrice(@PathVariable String productId,
                                                @RequestBody CreatePriceCommand cmd) {
        var fullCmd = new CreatePriceCommand(
                productId, cmd.currency(), cmd.amount(),
                cmd.billingInterval(), cmd.intervalCount(), cmd.isDefault(),
                cmd.condition(), cmd.attributes(), cmd.metadata(), cmd.tags()
        );
        var price = priceCommandService.create(fullCmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoAssembler.toDto(price));
    }

    @DeleteMapping("/prices/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable String id) {
        priceCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
