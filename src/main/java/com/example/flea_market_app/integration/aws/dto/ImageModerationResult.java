package com.example.flea_market_app.integration.aws.dto;

import java.util.List;

public record ImageModerationResult(
		boolean adult,
		boolean violence,
		double riskScore, // 0..1
		List<String> reasons) {
}
