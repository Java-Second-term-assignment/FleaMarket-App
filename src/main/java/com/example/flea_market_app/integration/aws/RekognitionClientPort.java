package com.example.flea_market_app.integration.aws;

import com.example.flea_market_app.integration.aws.dto.ImageModerationResult;

public interface RekognitionClientPort {
	ImageModerationResult analyzeImage(String s3ObjectKey);
}
