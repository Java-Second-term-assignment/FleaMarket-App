package com.example.flea_market_app.integration.aws;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.integration.aws.dto.FraudScore;
import com.example.flea_market_app.integration.aws.dto.FraudSignal;

@Component
public class FraudDetectorClientAdapter implements FraudDetectorClientPort {

	// private final FraudDetectorClient client; // 実採用SDKに合わせて注入

	@Override
	public FraudScore evaluate(FraudSignal signal) {
		try {
			// ここでAWSのAPIに合わせて attributes などを投入し、score を得る
			// double score = client.getPrediction(...);

			double score = pseudoScore(signal); // 仮実装
			return new FraudScore(score, "v1");
		} catch (RuntimeException e) {
			// ネットワーク系は Retryable に寄せてもよい
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		}
	}

	private double pseudoScore(FraudSignal signal) {
		// 開発用：属性に "risky" があればスコア上げる、など
		if (signal.attributes() != null && "true".equalsIgnoreCase(signal.attributes().get("risky")))
			return 0.9;
		return 0.1;
	}
}
