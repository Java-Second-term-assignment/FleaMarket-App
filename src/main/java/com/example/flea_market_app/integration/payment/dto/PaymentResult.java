package com.example.flea_market_app.integration.payment.dto;

public record PaymentResult(
		PaymentStatus status,
		String externalPaymentId) {
}
