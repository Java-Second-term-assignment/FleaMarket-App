package com.example.flea_market_app.transaction.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.engagement.notification.service.EmailNotificationSender;
import com.example.flea_market_app.transaction.domain.Order;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final EmailNotificationSender emailNotificationSender;

	@Transactional
	public void confirmPurchase(UUID orderId, UUID currentUserId) {
		OrderEntity e = getEntity(orderId);
		Order order = toDomain(e);

		order.assertBuyer(currentUserId);
		order.confirmPurchase();

		e.setStatus(order.getStatus().name());
		// updated_at はDBトリガが更新する想定だが、アプリ側で触っても害はない
		// e.setUpdatedAt(OffsetDateTime.now());

		orderRepository.save(e);
	}

	@Transactional
	public void notifyShipment(UUID orderId, UUID currentUserId) {
		OrderEntity e = getEntity(orderId);
		Order order = toDomain(e);

		order.assertSeller(currentUserId);
		order.ship(OffsetDateTime.now());

		e.setStatus(order.getStatus().name());
		orderRepository.save(e);
	}

	@Transactional
	public void confirmReceipt(UUID orderId, UUID currentUserId) {
		OrderEntity e = getEntity(orderId);
		Order order = toDomain(e);

		order.assertBuyer(currentUserId);
		order.complete(OffsetDateTime.now());

		e.setStatus(order.getStatus().name());
		orderRepository.save(e);

		// 将来:
		// - 完了イベント publish（通知・不正検知・監査）
		// - レビュー両者揃いで user ランク反映 など
	}

	@Transactional
	public void cancel(UUID orderId, UUID currentUserId, String reason) {
		OrderEntity e = getEntity(orderId);
		Order order = toDomain(e);

		order.assertParticipant(currentUserId);
		order.cancel();

		e.setStatus(order.getStatus().name());
		orderRepository.save(e);

		// 将来:
		// - reason を audit_logs / ユーザー操作ログへ残す（別Serviceに委譲推奨）
		// - Stripe返金などは PaymentClient 経由で transaction が要求する
	}

	/**
	 * 注文がPAIDになったタイミングで呼ぶ。売り手に取引成立メールを送る。
	 * 呼び出し元は決済完了（Stripe Webhook等）や注文作成APIを想定。今回の実装では呼び出し元は追加しない。
	 */
	@Transactional(readOnly = true)
	public void recordOrderPaid(UUID orderId) {
		OrderEntity e = getEntity(orderId);
		if (!OrderStatus.PAID.name().equals(e.getStatus())) {
			return;
		}
		emailNotificationSender.sendTransactionEstablished(e.getSellerId(), orderId);
	}

	private OrderEntity getEntity(UUID orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ORDER));
	}

	private Order toDomain(OrderEntity e) {
		return new Order(
				e.getId(),
				e.getItemId(),
				e.getBuyerId(),
				e.getSellerId(),
				OrderStatus.valueOf(e.getStatus()));
	}
}
