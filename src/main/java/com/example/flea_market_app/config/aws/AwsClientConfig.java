package com.example.flea_market_app.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

@Configuration
public class AwsClientConfig {

	@Bean
	public RekognitionClient rekognitionClient() {
		return RekognitionClient.builder()

				.region(Region.AP_NORTHEAST_1) // 現在は東京リージョンだが、修正必須(おそらく北バージニア)
				.build();

	}

}
