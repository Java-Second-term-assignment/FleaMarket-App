package com.example.flea_market_app.integration.aws;

import com.example.flea_market_app.integration.aws.dto.ModerationResult;

public interface ComprehendClientPort {
	ModerationResult analyzeText(String text);
}
