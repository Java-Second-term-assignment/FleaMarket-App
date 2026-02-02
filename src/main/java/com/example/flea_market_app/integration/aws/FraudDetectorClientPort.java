package com.example.flea_market_app.integration.aws;

import com.example.flea_market_app.integration.aws.dto.FraudScore;
import com.example.flea_market_app.integration.aws.dto.FraudSignal;

public interface FraudDetectorClientPort {
	FraudScore evaluate(FraudSignal signal);
}
