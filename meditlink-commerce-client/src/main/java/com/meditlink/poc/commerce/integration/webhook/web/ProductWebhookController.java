package com.meditlink.poc.commerce.integration.webhook.web;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/products")
public class ProductWebhookController {

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public WebhookAcceptedResponse receive(@RequestBody ProductWebhookEvent payload) {
        return new WebhookAcceptedResponse("accepted", payload.eventType(), Instant.now());
    }

    public record ProductWebhookEvent(String eventType, String payload) {
    }

    public record WebhookAcceptedResponse(String status, String eventType, Instant acceptedAt) {
    }
}
