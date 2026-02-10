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
import com.example.flea_market_app.engagement.notification.service.NotificationService;
import com.example.flea_market_app.integration.payment.PaymentClient;
import com.example.flea_market_app.integration.payment.StripePaymentClient;
import com.example.flea_market_app.integration.payment.dto.CheckoutPaymentRequest;
import com.example.flea_market_app.integration.payment.dto.PaymentRequest;
import com.example.flea_market_app.integration.payment.dto.PaymentResult;
import com.example.flea_market_app.transaction.domain.Order;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.repository.UserRepository;
import com.example.flea_market_app.user.service.UserRankService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;
	private final UserRankService userRankService;
	private final EmailNotificationSender emailNotificationSender;
	private final NotificationService notificationService;
	private final PaymentClient paymentClient;
	private final StripePaymentClient stripePaymentClient;

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
	 * 注文がPAIDになったタイミングで呼ぶ。売り手に取引成立メールと通知を送る。
	 * 呼び出し元は決済完了（Stripe Webhook等）または createOrderFromProduct（MVP）。
	 */
	@Transactional
	public void recordOrderPaid(UUID orderId) {
		OrderEntity e = getEntity(orderId);
		if (!OrderStatus.PAID.name().equals(e.getStatus())) {
			return;
		}
		UUID sellerId = e.getSellerId();
		emailNotificationSender.sendTransactionEstablished(sellerId, orderId);
		notificationService.create(sellerId, "取引が成立しました。注文ID: " + orderId);
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
		if (orderRepository.existsByItemId(itemId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.item_already_ordered");
		}
		if (item.getSellerId().equals(buyerId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.cannot_buy_own_item");
		}
		long price = item.getPriceAmount() != null ? item.getPriceAmount() : 0L;
		long shippingFee = 0L;
		long total = price + shippingFee;

		UUID sellerId = item.getSellerId();
		int commissionBps = 0;
		UserEntity seller = userRepository.findById(sellerId).orElse(null);
		if (seller != null) {
			UserRank rank = userRankService.loadRank(seller.getUserRankId());
			commissionBps = rank.getCommissionBps();
		}

		OrderEntity e = new OrderEntity();
		e.setId(UUID.randomUUID());
		e.setItemId(itemId);
		e.setBuyerId(buyerId);
		e.setSellerId(sellerId);
		e.setStripePaymentIntentId(null);
		e.setStatus(OrderStatus.PAID.name());
		e.setAppliedCommissionBps(commissionBps);
		e.setItemPriceAmount(price);
		e.setShippingFeeAmount(shippingFee);
		e.setTotalAmount(total);
		e.setCurrency(item.getCurrency() != null ? item.getCurrency() : "JPY");
		e.setShippingAddressSnapshot(addressSnapshot != null && !addressSnapshot.isEmpty() ? addressSnapshot : "{}");
		OffsetDateTime now = OffsetDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);

		orderRepository.save(e);
		recordOrderPaid(e.getId());
		return e.getId();
	}

	/**
	 * 商品1点から注文を1件作成する（Stripe用・status=PENDING）。決済完了は Webhook または markOrderPaidByStripe で PAID にする。
	 */
	@Transactional
	public UUID createOrderFromProductPending(UUID itemId, UUID buyerId, String addressSnapshot) {
		var item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (orderRepository.existsByItemId(itemId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.item_already_ordered");
		}
		if (item.getSellerId().equals(buyerId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.cannot_buy_own_item");
		}
		long price = item.getPriceAmount() != null ? item.getPriceAmount() : 0L;
		long shippingFee = 0L;
		long total = price + shippingFee;

		UUID sellerId = item.getSellerId();
		int commissionBps = 0;
		UserEntity seller = userRepository.findById(sellerId).orElse(null);
		if (seller != null) {
			UserRank rank = userRankService.loadRank(seller.getUserRankId());
			commissionBps = rank.getCommissionBps();
		}

		OrderEntity e = new OrderEntity();
		e.setId(UUID.randomUUID());
		e.setItemId(itemId);
		e.setBuyerId(buyerId);
		e.setSellerId(sellerId);
		e.setStripePaymentIntentId(null);
		e.setStatus(OrderStatus.PENDING.name());
		e.setAppliedCommissionBps(commissionBps);
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

	/**
	 * 注文を作らずに PaymentIntent のみ作成する（チェックアウト用）。決済完了後に Webhook または return URL で注文を作成する。
	 *
	 * @return PaymentResult（clientSecret と externalPaymentId = paymentIntentId）
	 */
	public PaymentResult createPaymentIntentForCheckout(UUID productId, UUID userId, String addressSnapshot) {
		var item = itemRepository.findById(productId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (orderRepository.existsByItemId(productId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.item_already_ordered");
		}
		if (item.getSellerId().equals(userId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.cannot_buy_own_item");
		}
		long amount = item.getPriceAmount() != null ? item.getPriceAmount() : 0L;
		String currency = item.getCurrency() != null && !item.getCurrency().isBlank() ? item.getCurrency() : "JPY";
		CheckoutPaymentRequest request = new CheckoutPaymentRequest(
				amount,
				currency,
				productId.toString(),
				userId.toString(),
				addressSnapshot != null ? addressSnapshot : "{}");
		PaymentResult result = stripePaymentClient.createPaymentIntentForCheckout(request);
		if (result.clientSecret() == null || result.clientSecret().isBlank()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.EXTERNAL_SERVICE_FAILED,
					"error.payment_failed");
		}
		return result;
	}

	/**
	 * 決済完了後に PaymentIntent の metadata から注文を 1 件作成する（冪等）。既に同一 payment_intent_id で注文があればその orderId を返す。
	 */
	@Transactional
	public UUID createOrderFromPaymentIntentMetadata(String paymentIntentId) {
		var existing = orderRepository.findByStripePaymentIntentId(paymentIntentId);
		if (existing.isPresent()) {
			return existing.get().getId();
		}
		java.util.Map<String, String> metadata = stripePaymentClient.retrievePaymentIntentMetadata(paymentIntentId);
		String productIdStr = metadata.get("productId");
		String buyerIdStr = metadata.get("buyerId");
		String addressSnapshot = metadata.get("addressSnapshot");
		if (productIdStr == null || productIdStr.isBlank() || buyerIdStr == null || buyerIdStr.isBlank()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.payment_metadata_invalid");
		}
		UUID itemId = UUID.fromString(productIdStr);
		UUID buyerId = UUID.fromString(buyerIdStr);
		var item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (orderRepository.existsByItemId(itemId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.item_already_ordered");
		}
		if (item.getSellerId().equals(buyerId)) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.cannot_buy_own_item");
		}
		long price = item.getPriceAmount() != null ? item.getPriceAmount() : 0L;
		long shippingFee = 0L;
		long total = price + shippingFee;
		UUID sellerId = item.getSellerId();
		int commissionBps = 0;
		UserEntity seller = userRepository.findById(sellerId).orElse(null);
		if (seller != null) {
			UserRank rank = userRankService.loadRank(seller.getUserRankId());
			commissionBps = rank.getCommissionBps();
		}
		OrderEntity e = new OrderEntity();
		e.setId(UUID.randomUUID());
		e.setItemId(itemId);
		e.setBuyerId(buyerId);
		e.setSellerId(sellerId);
		e.setStripePaymentIntentId(paymentIntentId);
		e.setStatus(OrderStatus.PAID.name());
		e.setAppliedCommissionBps(commissionBps);
		e.setItemPriceAmount(price);
		e.setShippingFeeAmount(shippingFee);
		e.setTotalAmount(total);
		e.setCurrency(item.getCurrency() != null ? item.getCurrency() : "JPY");
		e.setShippingAddressSnapshot(addressSnapshot != null && !addressSnapshot.isEmpty() ? addressSnapshot : "{}");
		OffsetDateTime now = OffsetDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		orderRepository.save(e);
		recordOrderPaid(e.getId());
		return e.getId();
	}

	/**
	 * 注文用の PaymentIntent を作成し、注文に stripe_payment_intent_id を保存する。呼び出し元は購入者であることの検証済みであること。
	 *
	 * @param orderId  PENDING の注文ID
	 * @param currency 通貨（未設定時は StripeProperties の currency を使う想定だが、ここでは注文の通貨を渡す）
	 * @return client_secret（フロントで Stripe.js に渡す）
	 */
	@Transactional
	public String createPaymentIntentForOrder(UUID orderId, String currency) {
		OrderEntity order = getEntity(orderId);
		if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_STATE,
					"error.order.not_pending");
		}
		long amount = order.getTotalAmount() != null ? order.getTotalAmount() : 0L;
		String cur = currency != null && !currency.isBlank() ? currency : (order.getCurrency() != null ? order.getCurrency() : "JPY");
		PaymentRequest request = new PaymentRequest(amount, cur, orderId.toString(), null);
		PaymentResult result = paymentClient.charge(request);
		if (result.clientSecret() == null || result.clientSecret().isBlank()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.EXTERNAL_SERVICE_FAILED,
					"error.payment_failed");
		}
		order.setStripePaymentIntentId(result.externalPaymentId());
		orderRepository.save(order);
		return result.clientSecret();
	}

	/**
	 * Stripe Webhook（payment_intent.succeeded）から呼ぶ。PENDING の注文を PAID に更新し、取引成立通知を送る。
	 */
	@Transactional
	public void markOrderPaidByStripe(UUID orderId) {
		OrderEntity e = getEntity(orderId);
		if (!OrderStatus.PENDING.name().equals(e.getStatus())) {
			return;
		}
		e.setStatus(OrderStatus.PAID.name());
		orderRepository.save(e);
		recordOrderPaid(orderId);
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
				OrderStatus.fromString(e.getStatus()));
	}
}
