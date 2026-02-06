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
		if (e.orderId() == null || e.orderId().isBlank()) {
			return;
		}

		// Stripe metadata の orderId は String なので UUID に変換
		final UUID orderId;
		try {
			orderId = UUID.fromString(e.orderId());
		} catch (IllegalArgumentException ex) {
			log.warn("Stripe webhook orderId is not a valid UUID: eventType={}, orderId={}", e.eventType(), e.orderId(), ex);
			return;
		}

		// 既存の OrderService API を呼ぶ（責務が合う）
		orderService.recordOrderPaid(orderId);
	}
}
