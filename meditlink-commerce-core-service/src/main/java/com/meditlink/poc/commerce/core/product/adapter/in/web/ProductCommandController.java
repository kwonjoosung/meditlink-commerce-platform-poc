package com.meditlink.poc.commerce.core.product.adapter.in.web;

import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductCommand;
import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.GetProductUseCase;
import com.meditlink.poc.commerce.core.product.domain.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Product 모듈의 HTTP Inbound Adapter
// - REST 요청을 use case 입력으로 변환
@RestController
@RequestMapping("/api/products")
public class ProductCommandController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;

    public ProductCommandController(CreateProductUseCase createProductUseCase, GetProductUseCase getProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product created = createProductUseCase.create(new CreateProductCommand(
                request.sku(),
                request.name(),
                request.basePrice(),
                request.currency()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        Optional<Product> product = getProductUseCase.getById(id);
        return product
                .map(value -> ResponseEntity.ok(ProductResponse.from(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record CreateProductRequest(
            @NotBlank String sku,
            @NotBlank String name,
            @PositiveOrZero long basePrice,
            @NotBlank String currency
    ) {
    }

    public record ProductResponse(
            Long id,
            String sku,
            String name,
            long basePrice,
            String currency
    ) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.id(),
                    product.sku(),
                    product.name(),
                    product.basePrice(),
                    product.currency()
            );
        }
    }
}
