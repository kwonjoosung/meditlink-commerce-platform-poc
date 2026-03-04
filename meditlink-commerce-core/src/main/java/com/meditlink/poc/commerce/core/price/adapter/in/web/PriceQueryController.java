package com.meditlink.poc.commerce.core.price.adapter.in.web;

import com.meditlink.poc.commerce.core.price.application.port.in.CalculatePriceUseCase;
import com.meditlink.poc.commerce.core.price.domain.PriceQuote;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
public class PriceQueryController {

    private final CalculatePriceUseCase calculatePriceUseCase;

    public PriceQueryController(CalculatePriceUseCase calculatePriceUseCase) {
        this.calculatePriceUseCase = calculatePriceUseCase;
    }

    @GetMapping("/{productId}")
    public PriceQuote calculate(
            @PathVariable Long productId,
            @RequestParam(required = false) String couponCode
    ) {
        return calculatePriceUseCase.calculate(productId, couponCode);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.notFound().build();
    }
}
