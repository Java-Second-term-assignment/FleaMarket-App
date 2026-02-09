package com.example.flea_market_app.integration.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.config.payment.StripeProperties;

class StripeWebhookVerifierTest {

	@Nested
	class WebhookSecretNotConfigured {
		@Test
		void whenSecretIsNull_throwsExternalServiceExceptionWithWebhookNotConfigured() {
			StripeProperties props = new StripeProperties(null, null, "JPY", null);
			StripeWebhookVerifier verifier = new StripeWebhookVerifier(props);

			assertThatThrownBy(() -> verifier.verify("{}", "stripe-signature"))
					.isInstanceOf(ExternalServiceException.class)
					.satisfies(ex -> {
						ExternalServiceException e = (ExternalServiceException) ex;
						assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WEBHOOK_NOT_CONFIGURED);
					});
		}

		@Test
		void whenSecretIsBlank_throwsExternalServiceExceptionWithWebhookNotConfigured() {
			StripeProperties props = new StripeProperties(null, "  ", "JPY", null);
			StripeWebhookVerifier verifier = new StripeWebhookVerifier(props);

			assertThatThrownBy(() -> verifier.verify("{}", "stripe-signature"))
					.isInstanceOf(ExternalServiceException.class)
					.satisfies(ex -> {
						ExternalServiceException ex2 = (ExternalServiceException) ex;
						assertThat(ex2.getErrorCode()).isEqualTo(ErrorCode.WEBHOOK_NOT_CONFIGURED);
					});
		}
	}

	@Nested
	class InvalidSignature {
		@Test
		void whenSignatureInvalid_throwsExternalServiceExceptionWithSignatureInvalid() {
			StripeProperties props = new StripeProperties(null, "whsec_test_secret", "JPY", null);
			StripeWebhookVerifier verifier = new StripeWebhookVerifier(props);

			assertThatThrownBy(() -> verifier.verify("{\"type\":\"event\"}", "invalid_signature"))
					.isInstanceOf(ExternalServiceException.class)
					.satisfies(ex -> {
						ExternalServiceException e = (ExternalServiceException) ex;
						assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WEBHOOK_SIGNATURE_INVALID);
					});
		}
	}
}
