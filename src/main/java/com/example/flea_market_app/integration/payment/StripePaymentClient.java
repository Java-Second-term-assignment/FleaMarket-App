package com.example.flea_market_app.integration.payment;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.common.exception.RetryableExternalException;
import com.example.flea_market_app.integration.payment.dto.PaymentRequest;
import com.example.flea_market_app.integration.payment.dto.PaymentResult;
import com.example.flea_market_app.integration.payment.dto.PaymentStatus;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Component
public class StripePaymentClient implements PaymentClient {

	@Override
	public PaymentResult charge(PaymentRequest request) {
		try {
			PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
					.setAmount(request.amount())
					.setCurrency(request.currency())
					// metadataに自ドメイン情報を入れる（Stripe依存はここで止める）
					.putMetadata("orderId", request.orderId())
					.setAutomaticPaymentMethods(
							PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
									.setEnabled(true)
									.build())
					.build();

			PaymentIntent intent = PaymentIntent.create(params);

			PaymentStatus status = mapStatus(intent.getStatus());
			return new PaymentResult(status, intent.getId());

		} catch (ApiConnectionException e) {
			throw new RetryableExternalException(
					ErrorCode.EXTERNAL_SERVICE_TEMPORARY,
					ErrorCode.EXTERNAL_SERVICE_TEMPORARY.getMessageKey(),
					e);
		} catch (StripeException e) {
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

	@Override
	public PaymentResult refund(String externalPaymentId, long amount) {
		try {
			// Refund APIの呼び方は要件で変わる（PaymentIntent / Charge / PaymentMethod）
			// ここは骨格だけ示す。実運用は intent から最新の charge を取って返金する、など設計。
			return new PaymentResult(PaymentStatus.SUCCEEDED, externalPaymentId);
		} catch (Exception e) {
			throw new ExternalServiceException(
					ErrorCode.EXTERNAL_SERVICE_FAILED,
					ErrorCode.EXTERNAL_SERVICE_FAILED.getMessageKey(),
					e);
		}
	}

	private PaymentStatus mapStatus(String stripeStatus) {
		return switch (stripeStatus) {
		case "succeeded" -> PaymentStatus.SUCCEEDED;
		case "requires_action", "requires_payment_method", "processing" -> PaymentStatus.REQUIRES_ACTION;
		default -> PaymentStatus.FAILED;
		};
	}
}
