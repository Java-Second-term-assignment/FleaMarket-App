package com.example.flea_market_app.transaction.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.transaction.service.OrderService;
import com.example.flea_market_app.transaction.service.ReviewService;
import com.example.flea_market_app.transaction.service.dto.CancelOrderRequest;
import com.example.flea_market_app.transaction.service.dto.CreateReviewRequest;

import lombok.RequiredArgsConstructor;

/**
 * 取引（契約）の操作を代表してHTTPとのやりとりを行う入口。
 *
 * 【絶対ルール】
 * - orderId は PathVariable（必須）
 * - userId は context から取得（リクエストで受け取らない）
 * - 認可・状態遷移ロジックは Service/Domain に集約（Controllerに if を増やさない）
 *
 * [想定API]
 * - 購入確定          POST /orders/{orderId}/confirm
 * - 発送通知          POST /orders/{orderId}/shipment
 * - 受取確認          POST /orders/{orderId}/receipt
 * - 取引キャンセル     POST /orders/{orderId}/cancel
 * - 評価登録          POST /orders/{orderId}/review
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

	private final OrderService orderService;
	private final ReviewService reviewService;

	/**
	 * 購入確定（購入者のみ）
	 *
	 * 典型:
	 * - 既に orders.status = PAID の注文が存在する
	 * - confirm により AWAITING_SHIPMENT へ進める
	 *
	 * 補足:
	 * - 本来「決済成功=PAID」は Stripe webhook で確定させるのが自然
	 * - MVPでは「PAIDの注文が存在する」前提で状態遷移に集中する
	 */
	@PostMapping("/{orderId}/confirm")
	public ResponseEntity<Void> confirm(@PathVariable UUID orderId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		orderService.confirmPurchase(orderId, userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 発送通知（売り手のみ）
	 *
	 * AWAITING_SHIPMENT -> SHIPPED
	 */
	@PostMapping("/{orderId}/shipment")
	public ResponseEntity<Void> shipment(@PathVariable UUID orderId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		orderService.notifyShipment(orderId, userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 受取確認（買い手のみ）
	 *
	 * SHIPPED -> COMPLETED
	 */
	@PostMapping("/{orderId}/receipt")
	public ResponseEntity<Void> receipt(@PathVariable UUID orderId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		orderService.confirmReceipt(orderId, userId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 取引キャンセル（条件付き）
	 *
	 * 原則:
	 * - 当事者のみ操作可
	 * - 発送後/完了後は不可（ドメイン側でブロック）
	 *
	 * 将来:
	 * - reason をユーザー操作ログへ残す
	 * - PaymentPort経由で返金/取消を要求する（transactionが入口）
	 */
	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<Void> cancel(
			@PathVariable UUID orderId,
			@Valid @RequestBody CancelOrderRequest req) {
		UUID userId = SecurityUtil.getCurrentUserId();
		orderService.cancel(orderId, userId, req.getReason());
		return ResponseEntity.noContent().build();
	}

	/**
	 * 取引完了後の評価登録
	 *
	 * ルール:
	 * - 当事者のみ
	 * - COMPLETED後のみ
	 * - 1取引につき reviewer は1回（DB UNIQUE(order_id, reviewer_id) + 事前チェック）
	 */
	@PostMapping("/{orderId}/review")
	public ResponseEntity<Void> review(
			@PathVariable UUID orderId,
			@Valid @RequestBody CreateReviewRequest req) {
		UUID userId = SecurityUtil.getCurrentUserId();
		reviewService.submitReview(orderId, userId, req.getRating(), req.getComment());
		return ResponseEntity.noContent().build();
	}

	// 将来:
	// - 注文詳細取得 GET /orders/{orderId}
	// - 自分の注文一覧 GET /orders?scope=buyer|seller
	// - 管理者介入 /orders/{orderId}/force-cancel（admin側へ）
}
