package com.example.flea_market_app.integration.aws;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.common.exception.RetryableExternalException;
import com.example.flea_market_app.integration.aws.dto.ModerationResult;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.DetectSentimentRequest;
import software.amazon.awssdk.services.comprehend.model.TextSizeLimitExceededException;

@Component
public class ComprehendClientAdapter implements ComprehendClientPort {

	private final ComprehendClient client;

	public ComprehendClientAdapter(ComprehendClient client) {
		this.client = client;
	}

	@Override
	public ModerationResult analyzeText(String text) {
		try {
			var req = DetectSentimentRequest.builder()
					.languageCode("ja") // Comprehendの制約により "ja" が使える想定
					.text(text)
					.build();

			var res = client.detectSentiment(req);

			double negative = res.sentimentScore() == null || res.sentimentScore().negative() == null
					? 0.0
					: res.sentimentScore().negative();

			boolean flagged = negative >= 0.85;

			return new ModerationResult(flagged, negative, List.of(res.sentimentAsString()));

		} catch (TextSizeLimitExceededException e) {
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		} catch (SdkClientException e) {
			throw new RetryableExternalException(
					ErrorCode.EXTERNAL_SERVICE_TEMPORARY,
					ErrorCode.EXTERNAL_SERVICE_TEMPORARY.getMessageKey(),
					e);
		} catch (Exception e) {
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		}
	}
}
