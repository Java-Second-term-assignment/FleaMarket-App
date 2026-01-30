package com.example.flea_market_app.listing.domain;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/**
 * 出品まわりのドメインモデル。バリデーションと下書き出品の生成を行う。
 */
@Getter
public class Listing {

	private final UUID id;
	private UUID copiedFromItemId;
	private String copySource;
	private UUID sellerId;
	private UUID categoryId;
	private String name;
	private String description;
	private long priceAmount;
	private String currency;
	private ItemStatus status;
	private ItemCondition condition;
	private ShippingFeePayer shippingFeePayer;

	/** 全フィールドを受け取るコンストラクタ。 */
	public Listing(UUID id, UUID copiedFromItemId, String copySource, UUID sellerId, UUID categoryId,
			String name, String description, long priceAmount, String currency,
			ItemStatus status, ItemCondition condition, ShippingFeePayer shippingFeePayer) {
		this.id = Objects.requireNonNull(id);
		this.copiedFromItemId = copiedFromItemId;
		this.copySource = copySource;
		this.sellerId = Objects.requireNonNull(sellerId);
		this.categoryId = Objects.requireNonNull(categoryId);
		this.name = Objects.requireNonNull(name);
		this.description = description != null ? description : "";
		this.priceAmount = priceAmount;
		this.currency = Objects.requireNonNull(currency);
		this.status = Objects.requireNonNull(status);
		this.condition = Objects.requireNonNull(condition);
		this.shippingFeePayer = Objects.requireNonNull(shippingFeePayer);
	}

	/**
	 * 下書き出品を生成する。
	 * 名前・説明・価格の簡易バリデーションを行う。
	 */
	public static Listing createDraft(UUID sellerId, UUID categoryId, String name, String description,
			long priceAmount, ItemCondition condition, ShippingFeePayer shippingFeePayer) {
		Objects.requireNonNull(sellerId);
		Objects.requireNonNull(categoryId);
		Objects.requireNonNull(condition);
		Objects.requireNonNull(shippingFeePayer);

		String n = requireNonBlank(name, "name");
		if (n.length() > 200) {
			throw new IllegalArgumentException("name must be <= 200 chars");
		}
		String desc = description == null ? "" : description.trim();
		if (desc.length() > 5000) {
			throw new IllegalArgumentException("description must be <= 5000 chars");
		}
		if (priceAmount < 0) {
			throw new IllegalArgumentException("priceAmount must be >= 0");
		}

		return new Listing(
				UUID.randomUUID(),
				null,
				null,
				sellerId,
				categoryId,
				n,
				desc,
				priceAmount,
				"JPY",
				ItemStatus.DRAFT,
				condition,
				shippingFeePayer);
	}

	// 公開時に実装予定
	// public void publish() { ... }

	private static String requireNonBlank(String v, String field) {
		if (v == null || v.trim().isEmpty()) {
			throw new IllegalArgumentException(field + " is required");
		}
		return v.trim();
	}
}
