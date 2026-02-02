package com.example.flea_market_app.config.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {
	public StripeConfig(StripeProperties props) {
		Stripe.apiKey = props.apiKey(); // SDKの初期化（ここだけ）
	}
}
