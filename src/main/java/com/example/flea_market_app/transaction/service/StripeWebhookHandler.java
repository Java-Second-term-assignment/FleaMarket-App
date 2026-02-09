package com.example.flea_market_app.transaction.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.integration.payment.VerifiedWebhook;

@Component
public class StripeWebhookHandler {

	private static final Logger log = LoggerFactory.getLogger(StripeWebhookHandler.class);

	private final OrderService orderService;

	public StripeWebhookHandler(OrderService orderService) {
		this.orderService = orderService;
	}

	@EventListener
	@Transactional
	public void onWebhook(VerifiedWebhook e) {
		if (!"payment_intent.succeeded".equals(e.eventType())) {
			return;
		}

		// 旧フロー: metadata に orderId がある場合は PENDING を PAID に更新
		if (e.orderId() != null && !e.orderId().isBlank()) {
			final UUID orderId;
			try {
				orderId = UUID.fromString(e.orderId());
			} catch (IllegalArgumentException ex) {
				log.warn("Stripe webhook orderId is not a valid UUID: eventType={}, orderId={}", e.eventType(), e.orderId(), ex);
				return;
			}
			orderService.markOrderPaidByStripe(orderId);
			return;
		}

		// 新フロー: metadata に productId/buyerId/addressSnapshot がある場合は決済完了後に注文を 1 件作成（冪等）
		if (e.productId() != null && !e.productId().isBlank() && e.externalPaymentId() != null && !e.externalPaymentId().isBlank()) {
			try {
				orderService.createOrderFromPaymentIntentMetadata(e.externalPaymentId());
			} catch (Exception ex) {
				log.warn("Stripe webhook createOrderFromPaymentIntentMetadata failed: paymentIntentId={}", e.externalPaymentId(), ex);
			}
		}
	}
}
