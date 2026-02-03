package com.example.flea_market_app.integration.payment;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.config.payment.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;

@Component
public class StripeWebhookVerifier implements WebhookVerifier {

	private final StripeProperties props;

	public StripeWebhookVerifier(StripeProperties props) {
		this.props = props;
	}

	@Override
	public VerifiedWebhook verify(String payload, String signatureHeader) {
		try {
			Event event = Webhook.constructEvent(payload, signatureHeader, props.webhookSecret());

			String type = event.getType();

			// 例：payment_intent.succeeded を扱う
			PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
					.getObject()
					.orElseThrow(() -> new ExternalServiceException(
							ErrorCode.EXTERNAL_SERVICE_FAILED,
							ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
							new IllegalStateException("stripe webhook data object missing")));

			String orderId = intent.getMetadata() == null ? null : intent.getMetadata().get("orderId");

			return new VerifiedWebhook(type, intent.getId(), orderId);

		} catch (SignatureVerificationException e) {
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		} catch (Exception e) {
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		}
	}
}
