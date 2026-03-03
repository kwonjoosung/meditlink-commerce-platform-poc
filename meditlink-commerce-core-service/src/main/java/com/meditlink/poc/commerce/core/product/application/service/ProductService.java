package com.meditlink.poc.commerce.core.product.application.service;

import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductCommand;
import com.meditlink.poc.commerce.core.product.application.port.in.CreateProductUseCase;
import com.meditlink.poc.commerce.core.product.application.port.in.GetProductUseCase;
import com.meditlink.poc.commerce.core.product.application.port.out.LoadProductPort;
import com.meditlink.poc.commerce.core.product.application.port.out.SaveProductPort;
import com.meditlink.poc.commerce.core.product.domain.Product;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Product 생성/단건조회 유스케이스 구현체
// - 포트에만 의존하고, 구체 기술(JPA 등)은 adapter.out으로 위임
@Service
@Transactional
public class ProductService implements CreateProductUseCase, GetProductUseCase {

    private final SaveProductPort saveProductPort;
    private final LoadProductPort loadProductPort;

    public ProductService(SaveProductPort saveProductPort, LoadProductPort loadProductPort) {
        this.saveProductPort = saveProductPort;
        this.loadProductPort = loadProductPort;
    }

    @Override
    public Product create(CreateProductCommand command) {
        String currency = command.currency() == null || command.currency().isBlank() ? "KRW" : command.currency();
        Product newProduct = Product.newProduct(
                command.sku(),
                command.name(),
                command.basePrice(),
                currency
        );
        return saveProductPort.save(newProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getById(Long productId) {
        return loadProductPort.loadById(productId);
    }
}
