package com.example.flea_market_app.transaction.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
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
	private final ItemRepository itemRepository;
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

	/**
	 * 商品1点から注文を1件作成する（MVP用・status=PAID）。
	 * 本番では Stripe 連携時に「注文作成 → PaymentIntent → 決済 → Webhook」に差し替える想定。
	 *
	 * @param itemId           商品ID
	 * @param buyerId          購入者ID
	 * @param addressSnapshot  JSON形式の配送先スナップショット（NOT NULL）
	 * @return 作成した注文のID
	 */
	@Transactional
	public UUID createOrderFromProduct(UUID itemId, UUID buyerId, String addressSnapshot) {
		var item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (item.getSellerId().equals(buyerId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.cannot_buy_own_item");
		}
		long price = item.getPriceAmount() != null ? item.getPriceAmount() : 0L;
		long shippingFee = 0L;
		long total = price + shippingFee;

		OrderEntity e = new OrderEntity();
		e.setId(UUID.randomUUID());
		e.setItemId(itemId);
		e.setBuyerId(buyerId);
		e.setSellerId(item.getSellerId());
		e.setStripePaymentIntentId(null);
		e.setStatus(OrderStatus.PAID.name());
		e.setAppliedCommissionBps(0);
		e.setItemPriceAmount(price);
		e.setShippingFeeAmount(shippingFee);
		e.setTotalAmount(total);
		e.setCurrency(item.getCurrency() != null ? item.getCurrency() : "JPY");
		e.setShippingAddressSnapshot(addressSnapshot != null && !addressSnapshot.isEmpty() ? addressSnapshot : "{}");
		OffsetDateTime now = OffsetDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);

		orderRepository.save(e);
		return e.getId();
	}

	private OrderEntity getEntity(UUID orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ORDER));
	}

	private Order toDomain(OrderEntity e) {
		String statusStr = e.getStatus();
		if (statusStr == null) {
			throw new IllegalStateException("Order status is null: orderId=" + e.getId());
		}
		return new Order(
				e.getId(),
				e.getItemId(),
				e.getBuyerId(),
				e.getSellerId(),
				OrderStatus.valueOf(statusStr));
	}
}
