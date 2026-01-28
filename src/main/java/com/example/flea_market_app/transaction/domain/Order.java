package com.example.flea_market_app.transaction.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

import lombok.Getter;

/**
 * 取引（契約）の全状態を管理するドメイン。
 *
 * 保持するべきもの（最小）:
 * - buyerId / sellerId
 * - status
 *
 * メソッド:
 * - confirmPurchase()
 * - ship()
 * - complete()
 * - cancel()
 * - assertParticipant()
 * - assertSeller()
 * - assertBuyer()
 *
 * 補足:
 * - DB側は orders に金額/住所スナップショットも持つが、
 *   状態遷移のルールはドメインに閉じる。
 */
@Getter
public class Order {

	private final UUID id;
	private final UUID itemId;
	private final UUID buyerId;
	private final UUID sellerId;

	private OrderStatus status;

	// 将来: 双方レビュー整合をドメインで表現したい場合に使用（MVPでは必須ではない）
	private boolean reviewedByBuyer;
	private boolean reviewedBySeller;

	private OffsetDateTime shippedAt;
	private OffsetDateTime completedAt;

	public Order(UUID id, UUID itemId, UUID buyerId, UUID sellerId, OrderStatus status) {
		this.id = Objects.requireNonNull(id);
		this.itemId = Objects.requireNonNull(itemId);
		this.buyerId = Objects.requireNonNull(buyerId);
		this.sellerId = Objects.requireNonNull(sellerId);
		this.status = Objects.requireNonNull(status);
	}

	public void assertParticipant(UUID userId) {
		Objects.requireNonNull(userId);
		if (!buyerId.equals(userId) && !sellerId.equals(userId)) {
			throw new AccessDeniedBusinessException();
		}
	}

	public void assertSeller(UUID userId) {
		Objects.requireNonNull(userId);
		if (!sellerId.equals(userId)) {
			throw new AccessDeniedBusinessException();
		}
	}

	public void assertBuyer(UUID userId) {
		Objects.requireNonNull(userId);
		if (!buyerId.equals(userId)) {
			throw new AccessDeniedBusinessException();
		}
	}

	public void confirmPurchase() {
		if (status != OrderStatus.PAID) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.order.not_paid");
		}
		status = OrderStatus.AWAITING_SHIPMENT;
	}

	public void ship(OffsetDateTime now) {
		Objects.requireNonNull(now);

		if (status != OrderStatus.AWAITING_SHIPMENT) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.order.not_awaiting_shipment");
		}
		status = OrderStatus.SHIPPED;
		shippedAt = now;
	}

	public void complete(OffsetDateTime now) {
		Objects.requireNonNull(now);

		if (status != OrderStatus.SHIPPED) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.order.not_shipped");
		}
		status = OrderStatus.COMPLETED;
		completedAt = now;
	}

	public void cancel() {
		if (status == OrderStatus.SHIPPED || status == OrderStatus.COMPLETED) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.order.cannot_cancel_after_shipment");
		}
		status = OrderStatus.CANCELLED;
	}

	// 将来: 片側レビューの記録を持つなら使う
	public void markReviewedByBuyer() {
		reviewedByBuyer = true;
	}

	public void markReviewedBySeller() {
		reviewedBySeller = true;
	}

	public boolean isBothReviewed() {
		return reviewedByBuyer && reviewedBySeller;
	}
}
