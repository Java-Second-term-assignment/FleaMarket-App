package com.example.flea_market_app.config.aws;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.flea_market_app.integration.aws.ComprehendClient;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
// FraudDetectorは環境により service 名が異なるため、ここは仮。実採用SDKに合わせて差し替え。

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfig {

	@Bean
	ComprehendClient comprehendClient(AwsProperties props) {
		return ComprehendClient.builder()
				.region(Region.of(props.region()))
				.build();
	}

	@Bean
	RekognitionClient rekognitionClient(AwsProperties props) {
		return RekognitionClient.builder()
				.region(Region.of(props.region()))
				.build();
	}
}
