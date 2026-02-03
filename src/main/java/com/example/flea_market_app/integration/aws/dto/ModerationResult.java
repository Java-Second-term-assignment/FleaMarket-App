package com.example.flea_market_app.integration.aws.dto;

import java.util.List;

public record ModerationResult(
		boolean flagged,
		double negativeScore, // 0..1
		List<String> labels // "HATE", "INSULT" など（自前ルールでOK）
) {
}
