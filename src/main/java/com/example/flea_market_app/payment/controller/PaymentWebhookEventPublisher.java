package com.example.flea_market_app.payment.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.flea_market_app.integration.payment.VerifiedWebhook;

@Component
public class PaymentWebhookEventPublisher {
	private final ApplicationEventPublisher publisher;

	public PaymentWebhookEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	public void publish(VerifiedWebhook verified) {
		publisher.publishEvent(verified);
	}
}
