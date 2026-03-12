package com.meditlink.poc.commerce.core.product.application.command;

import com.meditlink.poc.commerce.core.product.application.dto.AddFeatureCommand;
import com.meditlink.poc.commerce.core.product.application.dto.CreateProductCommand;
import com.meditlink.poc.commerce.core.product.application.dto.UpdateProductCommand;
import com.meditlink.poc.commerce.core.product.application.port.ProductGroupRepository;
import com.meditlink.poc.commerce.core.product.application.port.ProductRepository;
import com.meditlink.poc.commerce.core.product.application.port.StripeProductSync;
import com.meditlink.poc.commerce.core.product.domain.product.Product;
import com.meditlink.poc.commerce.core.product.infrastructure.kafka.ProductEventPublisher;
import com.meditlink.poc.commerce.core.shared.domain.ProductGroupId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final StripeProductSync stripeProductSync;
    private final ProductEventPublisher eventPublisher;

    public ProductCommandService(ProductRepository productRepository,
                                 ProductGroupRepository productGroupRepository,
                                 StripeProductSync stripeProductSync,
                                 ProductEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.productGroupRepository = productGroupRepository;
        this.stripeProductSync = stripeProductSync;
        this.eventPublisher = eventPublisher;
    }

    public Product create(CreateProductCommand cmd) {
        // ProductGroup 존재 검증
        productGroupRepository.findById(ProductGroupId.of(cmd.productGroupId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProductGroup을 찾을 수 없습니다: " + cmd.productGroupId()));

        var product = Product.create(
                ProductGroupId.of(cmd.productGroupId()),
                cmd.name(), cmd.displayName(), cmd.description(), cmd.itemType()
        );

        // Stripe 동기화 → externalId 할당
        String externalId = stripeProductSync.syncProduct(product);
        product.assignExternalId(externalId);

        var saved = productRepository.save(product);
        eventPublisher.publishProductCreated(saved);
        return saved;
    }

    public Product update(String id, UpdateProductCommand cmd) {
        var product = findOrThrow(id);

        product.updateInfo(cmd.name(), cmd.displayName(), cmd.description());
        product.updateTierOrder(cmd.tierOrder());
        product.updateVisibility(cmd.visibility());
        product.updateDisplayConfig(cmd.displayConfig());
        product.updateVisibilityRules(cmd.visibilityRules());
        product.updateCompatibility(cmd.compatibility());

        var saved = productRepository.save(product);
        eventPublisher.publishProductUpdated(saved);
        return saved;
    }

    public Product addFeature(String productId, AddFeatureCommand cmd) {
        var product = findOrThrow(productId);
        product.addFeature(cmd.featureCode(), cmd.quota(), cmd.displayLabel(), cmd.isHighlighted());
        return productRepository.save(product);
    }

    public Product removeFeature(String productId, String featureCode) {
        var product = findOrThrow(productId);
        product.removeFeature(featureCode);
        return productRepository.save(product);
    }

    public Product deactivate(String id) {
        var product = findOrThrow(id);
        product.deactivate();
        var saved = productRepository.save(product);
        eventPublisher.publishProductUpdated(saved);
        return saved;
    }

    public void delete(String id) {
        productRepository.deleteById(ProductId.of(id));
    }

    private Product findOrThrow(String id) {
        return productRepository.findById(ProductId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Product를 찾을 수 없습니다: " + id));
    }
}
