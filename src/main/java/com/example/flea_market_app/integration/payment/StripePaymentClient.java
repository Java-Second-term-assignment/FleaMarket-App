package com.example.flea_market_app.integration.payment;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ExternalServiceException;
import com.example.flea_market_app.common.exception.RetryableExternalException;
import com.example.flea_market_app.integration.payment.dto.CheckoutPaymentRequest;
import com.example.flea_market_app.integration.payment.dto.PaymentRequest;
import com.example.flea_market_app.integration.payment.dto.PaymentResult;
import com.example.flea_market_app.integration.payment.dto.PaymentStatus;
import java.util.Map;
import java.util.stream.Collectors;

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
			return new PaymentResult(status, intent.getId(), intent.getClientSecret());

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

	/**
	 * 注文を作らずに PaymentIntent のみ作成する（チェックアウト用）。
	 * metadata に productId, buyerId, addressSnapshot を設定。決済成功後に Webhook または return URL で注文を作成する。
	 */
	public PaymentResult createPaymentIntentForCheckout(CheckoutPaymentRequest request) {
		try {
			PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
					.setAmount(request.amount())
					.setCurrency(request.currency() != null ? request.currency() : "JPY")
					.putMetadata("productId", request.productId() != null ? request.productId() : "")
					.putMetadata("buyerId", request.buyerId() != null ? request.buyerId() : "")
					.putMetadata("addressSnapshot", request.addressSnapshot() != null ? request.addressSnapshot() : "{}")
					.setAutomaticPaymentMethods(
							PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
									.setEnabled(true)
									.build());
			PaymentIntentCreateParams params = paramsBuilder.build();
			PaymentIntent intent = PaymentIntent.create(params);
			PaymentStatus status = mapStatus(intent.getStatus());
			return new PaymentResult(status, intent.getId(), intent.getClientSecret());
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

	/**
	 * PaymentIntent を取得し、metadata を返す。決済完了後に注文を作成する際に使用。
	 */
	public Map<String, String> retrievePaymentIntentMetadata(String paymentIntentId) {
		if (paymentIntentId == null || paymentIntentId.isBlank()) {
			throw new IllegalArgumentException("paymentIntentId is required");
		}
		try {
			PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
			Map<String, String> metadata = intent.getMetadata();
			if (metadata == null) {
				return Map.of();
			}
			return metadata.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue() : ""));
		} catch (StripeException e) {
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
			return new PaymentResult(PaymentStatus.SUCCEEDED, externalPaymentId, null);
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
