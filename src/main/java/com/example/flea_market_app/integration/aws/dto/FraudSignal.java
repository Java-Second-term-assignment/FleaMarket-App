package com.example.flea_market_app.integration.aws.dto;

import java.util.Map;

public record FraudSignal(
		String eventType, // "order_created" 等
		String userId,
		String listingId,
		String transactionId,
		Map<String, String> attributes // 任意の特徴量
) {
}
