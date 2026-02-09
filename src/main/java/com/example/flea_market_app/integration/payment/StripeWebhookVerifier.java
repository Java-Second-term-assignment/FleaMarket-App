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
		String secret = props.webhookSecret();
		if (secret == null || secret.isBlank()) {
			throw new ExternalServiceException(
					ErrorCode.WEBHOOK_NOT_CONFIGURED,
					ErrorCode.WEBHOOK_NOT_CONFIGURED.getMessageKey());
		}
		try {
			Event event = Webhook.constructEvent(payload, signatureHeader, secret);

			String type = event.getType();

			// 例：payment_intent.succeeded を扱う
			PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
					.getObject()
					.orElseThrow(() -> new ExternalServiceException(
							ErrorCode.EXTERNAL_SERVICE_FAILED,
							ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
							new IllegalStateException("stripe webhook data object missing")));

			java.util.Map<String, String> meta = intent.getMetadata();
			String orderId = meta == null ? null : meta.get("orderId");
			String productId = meta == null ? null : meta.get("productId");
			String buyerId = meta == null ? null : meta.get("buyerId");
			String addressSnapshot = meta == null ? null : meta.get("addressSnapshot");
			return new VerifiedWebhook(type, intent.getId(), orderId, productId, buyerId, addressSnapshot);

		} catch (SignatureVerificationException e) {
			throw new ExternalServiceException(
					ErrorCode.WEBHOOK_SIGNATURE_INVALID,
					ErrorCode.WEBHOOK_SIGNATURE_INVALID.getMessageKey(),
					e);
		} catch (Exception e) {
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		}
	}
}
