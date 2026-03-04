package com.meditlink.poc.commerce.core.price.application.service;

import com.meditlink.poc.commerce.core.price.application.port.in.CalculatePriceUseCase;
import com.meditlink.poc.commerce.core.price.domain.PriceQuote;
import com.meditlink.poc.commerce.core.product.application.port.out.LoadProductPort;
import com.meditlink.poc.commerce.core.product.domain.Product;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PriceService implements CalculatePriceUseCase {

    private final LoadProductPort loadProductPort;

    public PriceService(LoadProductPort loadProductPort) {
        this.loadProductPort = loadProductPort;
    }

    @Override
    public PriceQuote calculate(Long productId, String couponCode) {
        Product product = loadProductPort.loadById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        long finalPrice = product.basePrice();
        if (couponCode != null && couponCode.startsWith("SALE10")) {
            finalPrice = Math.round(product.basePrice() * 0.9);
        }

        return new PriceQuote(product.id(), finalPrice, product.currency(), couponCode);
    }
}
