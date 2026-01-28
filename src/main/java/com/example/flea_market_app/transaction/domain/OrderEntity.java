package com.example.flea_market_app.transaction.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * DB: orders
 *
 * 注意:
 * - NOT NULL が多い（commission/amount/address 等）
 * - “注文作成”のユースケースを入れるなら、必ずここを埋めて INSERT すること
 *
 * MVP運用:
 * - 既に orders が存在する前提で status 遷移だけ行ってもOK
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "item_id", nullable = false, unique = true, columnDefinition = "uuid")
	private UUID itemId;

	@Column(name = "buyer_id", nullable = false, columnDefinition = "uuid")
	private UUID buyerId;

	@Column(name = "seller_id", nullable = false, columnDefinition = "uuid")
	private UUID sellerId;

	@Column(name = "stripe_payment_intent_id", unique = true)
	private String stripePaymentIntentId;

	@Column(nullable = false)
	private String status; // OrderStatus.name()

	@Column(name = "applied_commission_bps", nullable = false)
	private Integer appliedCommissionBps;

	@Column(name = "item_price_amount", nullable = false)
	private Long itemPriceAmount;

	@Column(name = "shipping_fee_amount", nullable = false)
	private Long shippingFeeAmount;

	@Column(name = "total_amount", nullable = false)
	private Long totalAmount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "shipping_address_snapshot", nullable = false, columnDefinition = "jsonb")
	private String shippingAddressSnapshot;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
}
