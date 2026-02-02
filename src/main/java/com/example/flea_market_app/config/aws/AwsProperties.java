package com.example.flea_market_app.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public record AwsProperties(
		String region,
		String s3Bucket) {
}
