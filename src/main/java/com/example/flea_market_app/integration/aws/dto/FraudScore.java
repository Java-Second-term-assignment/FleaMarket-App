package com.example.flea_market_app.integration.aws.dto;

public record FraudScore(
		double score, // 0..1
		String modelVersion) {
}
