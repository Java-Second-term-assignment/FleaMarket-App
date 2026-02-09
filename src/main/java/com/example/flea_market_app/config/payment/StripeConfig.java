package com.example.flea_market_app.config.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {
	public StripeConfig(StripeProperties props) {
		// 擬似決済用: apiKey が設定されているときだけ Stripe SDK を初期化（未設定時は従来フローのみ）
		if (props.apiKey() != null && !props.apiKey().isBlank()) {
			Stripe.apiKey = props.apiKey();
		}
	}
}
