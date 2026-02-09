package com.example.flea_market_app.integration.payment.dto;

public record PaymentResult(
		PaymentStatus status,
		String externalPaymentId,
		String clientSecret) {

	/** Refund 等で clientSecret が不要な場合用 */
	public PaymentResult(PaymentStatus status, String externalPaymentId) {
		this(status, externalPaymentId, null);
	}
}
