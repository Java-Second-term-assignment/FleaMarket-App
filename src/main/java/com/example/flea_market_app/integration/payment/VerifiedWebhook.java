package com.example.flea_market_app.integration.payment;

/**
 * Stripe Webhook 検証済みペイロード。orderId は旧フロー（注文先行）、productId/buyerId/addressSnapshot は新フロー（決済完了後に注文作成）。
 */
public record VerifiedWebhook(
		String eventType,
		String externalPaymentId,
		String orderId,
		String productId,
		String buyerId,
		String addressSnapshot) {

	public VerifiedWebhook(String eventType, String externalPaymentId, String orderId) {
		this(eventType, externalPaymentId, orderId, null, null, null);
	}
}
