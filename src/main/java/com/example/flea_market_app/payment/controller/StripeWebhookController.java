package com.example.flea_market_app.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.integration.payment.WebhookVerifier;

@RestController
@RequestMapping("/webhooks/stripe")
public class StripeWebhookController {

	private final WebhookVerifier verifier;
	private final PaymentWebhookEventPublisher publisher;

	public StripeWebhookController(WebhookVerifier verifier, PaymentWebhookEventPublisher publisher) {
		this.verifier = verifier;
		this.publisher = publisher;
	}

	@PostMapping
	public ResponseEntity<Void> handle(
			@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String sig) {
		var verified = verifier.verify(payload, sig);
		publisher.publish(verified); // ← ここから service に渡す（イベント）
		return ResponseEntity.ok().build();
	}
}
