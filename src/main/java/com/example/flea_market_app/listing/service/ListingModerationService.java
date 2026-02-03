package com.example.flea_market_app.listing.service;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.integration.aws.ComprehendClientPort;
import com.example.flea_market_app.integration.aws.RekognitionClientPort;

@Service
public class ListingModerationService {

	private final ComprehendClientPort comprehend;
	private final RekognitionClientPort rekognition;

	public ListingModerationService(ComprehendClientPort comprehend, RekognitionClientPort rekognition) {
		this.comprehend = comprehend;
		this.rekognition = rekognition;
	}

	public ModerationDecision decide(String description, String imageS3Key) {
		var text = comprehend.analyzeText(description);
		var img = rekognition.analyzeImage(imageS3Key);

		// 出品のAI一時判定は最終判断はここで実施する（service/domain）
		boolean reject = text.flagged() || img.adult() || img.violence() || img.riskScore() >= 0.85;

		return new ModerationDecision(reject, text, img);
	}

	public record ModerationDecision(
			boolean reject,
			Object textResult,
			Object imageResult) {
	}
}
