package com.example.flea_market_app.integration.payment;

public record VerifiedWebhook(
		String eventType,
		String externalPaymentId,
		String orderId) {
}
