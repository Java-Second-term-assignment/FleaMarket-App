package com.example.flea_market_app.config.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(
        String apiKey,
        String webhookSecret,
        String currency
) {}
