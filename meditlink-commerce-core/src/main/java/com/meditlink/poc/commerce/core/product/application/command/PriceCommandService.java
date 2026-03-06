package com.meditlink.poc.commerce.core.product.application.command;

import com.meditlink.poc.commerce.core.product.application.dto.CreatePriceCommand;
import com.meditlink.poc.commerce.core.product.application.port.PriceRepository;
import com.meditlink.poc.commerce.core.product.application.port.ProductRepository;
import com.meditlink.poc.commerce.core.product.application.port.StripePriceSync;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import com.meditlink.poc.commerce.core.shared.infra.rule.RuleDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceCommandService {

    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;
    private final StripePriceSync stripePriceSync;

    public PriceCommandService(PriceRepository priceRepository,
                               ProductRepository productRepository,
                               StripePriceSync stripePriceSync) {
        this.priceRepository = priceRepository;
        this.productRepository = productRepository;
        this.stripePriceSync = stripePriceSync;
    }

    public Price create(CreatePriceCommand cmd) {
        // Product 존재 + externalId 확보
        var product = productRepository.findById(ProductId.of(cmd.productId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product를 찾을 수 없습니다: " + cmd.productId()));

        var price = Price.create(
                ProductId.of(cmd.productId()),
                cmd.currency(), cmd.amount(),
                cmd.billingInterval(), cmd.intervalCount(), cmd.isDefault()
        );

        if (cmd.condition() != null) {
            price.updateCondition(RuleDeserializer.deserialize(cmd.condition()));
        }
        if (cmd.attributes() != null) price.updateAttributes(cmd.attributes());
        if (cmd.metadata() != null) price.updateMetadata(cmd.metadata());
        if (cmd.tags() != null) price.updateTags(cmd.tags());

        // Stripe 동기화 → externalId 할당
        String externalId = stripePriceSync.syncPrice(price, product.getExternalId());
        price.assignExternalId(externalId);

        return priceRepository.save(price);
    }

    public void delete(String id) {
        priceRepository.deleteById(PriceId.of(id));
    }
}
