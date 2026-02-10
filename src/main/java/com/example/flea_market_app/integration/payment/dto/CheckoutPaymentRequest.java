package com.example.flea_market_app.integration.payment.dto;

/**
 * 決済完了前に注文を作らないチェックアウト用。PaymentIntent の metadata に productId, buyerId, addressSnapshot を渡す。
 */
public record CheckoutPaymentRequest(
		long amount,
		String currency,
		String productId,
		String buyerId,
		String addressSnapshot
) {}
