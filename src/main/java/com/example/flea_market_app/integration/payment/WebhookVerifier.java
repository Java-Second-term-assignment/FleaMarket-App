package com.example.flea_market_app.integration.payment;

public interface WebhookVerifier {
	VerifiedWebhook verify(String payload, String signatureHeader);
}
