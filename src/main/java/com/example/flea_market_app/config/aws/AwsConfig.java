package com.example.flea_market_app.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
// FraudDetectorは環境により service 名が異なるため、ここは仮。実採用SDKに合わせて差し替え。

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfig {

	@Bean
	S3Client s3Client(@Value("${aws.s3.region:ap-northeast-1}") String region) {
		return S3Client.builder()
				.region(Region.of(region))
				.build();
	}

	@Bean
	ComprehendClient comprehendClient(@Value("${aws.s3.region:ap-northeast-1}") String region) {
		return ComprehendClient.builder()
				.region(Region.of(region))
				.build();
	}

	@Bean
	RekognitionClient rekognitionClient(@Value("${aws.s3.region:ap-northeast-1}") String region) {
		return RekognitionClient.builder()
				.region(Region.of(region))
				.build();
	}
}
