package com.example.flea_market_app.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.flea_market_app.integration.payment.VerifiedWebhook;
import com.example.flea_market_app.integration.payment.WebhookVerifier;
import com.example.flea_market_app.payment.controller.PaymentWebhookEventPublisher;
import com.example.flea_market_app.support.IntegrationTestBase;

class StripeWebhookIntegrationTests extends IntegrationTestBase {

	@MockitoBean
	WebhookVerifier webhookVerifier;

	@MockitoBean
	PaymentWebhookEventPublisher paymentWebhookEventPublisher;

	@Nested
	class Handle {

		@Test
		void handle_withoutSignature_returns400() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> request = new HttpEntity<>("{\"type\":\"payment_intent.succeeded\"}", headers);

			var response = restTemplate.exchange(
					"/webhooks/stripe",
					HttpMethod.POST,
					request,
					Void.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		}

		@Test
		void handle_withInvalidSignature_callsVerifierAndReturns401() {
			when(webhookVerifier.verify(anyString(), anyString()))
					.thenThrow(new com.example.flea_market_app.common.exception.ExternalServiceException(
							com.example.flea_market_app.common.error.ErrorCode.WEBHOOK_SIGNATURE_INVALID,
							"error.webhook_signature_invalid"));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Stripe-Signature", "invalid-signature");
			HttpEntity<String> request = new HttpEntity<>("{\"type\":\"payment_intent.succeeded\"}", headers);

			var response = restTemplate.exchange(
					"/webhooks/stripe",
					HttpMethod.POST,
					request,
					java.util.Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		@Test
		void handle_withValidSignature_returns200() {
			VerifiedWebhook verified = new VerifiedWebhook("payment_intent.succeeded", "pi_xxx", "order-123");
			when(webhookVerifier.verify(anyString(), anyString())).thenReturn(verified);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Stripe-Signature", "valid-signature");
			HttpEntity<String> request = new HttpEntity<>("{\"type\":\"payment_intent.succeeded\"}", headers);

			var response = restTemplate.exchange(
					"/webhooks/stripe",
					HttpMethod.POST,
					request,
					Void.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			verify(webhookVerifier).verify(anyString(), anyString());
			verify(paymentWebhookEventPublisher).publish(verified);
		}
	}
}
