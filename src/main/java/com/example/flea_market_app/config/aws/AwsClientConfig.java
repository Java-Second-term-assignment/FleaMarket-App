package com.example.flea_market_app.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWSクライアントの設定クラス。
 * 
 * <p>RekognitionClient、S3ClientなどのAWS SDKクライアントをBeanとして定義します。
 * リージョンなどの設定はapplication.propertiesから読み込みます。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Configuration
public class AwsClientConfig {

	@Value("${aws.s3.region:ap-northeast-1}")
	private String s3Region;

	/**
	 * RekognitionClientのBean定義。
	 * 
	 * @return RekognitionClientインスタンス
	 */
	@Bean
	RekognitionClient rekognitionClient() {
		return RekognitionClient.builder()
				.region(Region.AP_NORTHEAST_1) // 現在は東京リージョンだが、修正必須(おそらく北バージニア)
				.build();
	}

	/**
	 * S3ClientのBean定義。
	 * 
	 * <p>リージョンはapplication.propertiesのaws.s3.regionから読み込みます。
	 * デフォルトはap-northeast-1（東京リージョン）です。
	 * 
	 * @return S3Clientインスタンス
	 */
	@Bean
	S3Client s3Client() {
		return S3Client.builder()
				.region(Region.of(s3Region))
				.build();
	}

}
