package com.meditlink.poc.commerce.core.product.application.command;

import com.meditlink.poc.commerce.core.product.application.dto.CreatePriceCommand;
import com.meditlink.poc.commerce.core.product.application.port.PriceRepository;
import com.meditlink.poc.commerce.core.product.application.port.ProductRepository;
import com.meditlink.poc.commerce.common.proto.v1.PriceChangedEvent;
import com.meditlink.poc.commerce.core.product.application.port.StripePriceSync;
import com.meditlink.poc.commerce.core.product.domain.price.Price;
import com.meditlink.poc.commerce.core.product.infrastructure.kafka.ProductEventPublisher;
import com.meditlink.poc.commerce.core.shared.domain.PriceId;
import com.meditlink.poc.commerce.core.shared.domain.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceCommandService {

    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;
    private final StripePriceSync stripePriceSync;
    private final ProductEventPublisher eventPublisher;

    public PriceCommandService(PriceRepository priceRepository,
                               ProductRepository productRepository,
                               StripePriceSync stripePriceSync,
                               ProductEventPublisher eventPublisher) {
        this.priceRepository = priceRepository;
        this.productRepository = productRepository;
        this.stripePriceSync = stripePriceSync;
        this.eventPublisher = eventPublisher;
    }

    public Price create(CreatePriceCommand cmd) {
        // Product 존재 + externalId 확보
        var product = productRepository.findById(ProductId.of(cmd.productId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product를 찾을 수 없습니다: " + cmd.productId()));

        var price = Price.create(
                ProductId.of(cmd.productId()),
                cmd.currency(), cmd.amount(),
                cmd.billingPeriod(), cmd.isDefault()
        );

        // Stripe 동기화 → externalId 할당
        String externalId = stripePriceSync.syncPrice(price, product.getExternalId());
        price.assignExternalId(externalId);

        var saved = priceRepository.save(price);
        eventPublisher.publishPriceChanged(saved, PriceChangedEvent.ChangeType.CREATED);
        return saved;
    }

    public void delete(String id) {
        var price = priceRepository.findById(PriceId.of(id)).orElse(null);
        priceRepository.deleteById(PriceId.of(id));
        if (price != null) {
            eventPublisher.publishPriceChanged(price, PriceChangedEvent.ChangeType.DELETED);
        }
    }
}
