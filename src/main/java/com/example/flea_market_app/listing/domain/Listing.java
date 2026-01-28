package com.example.flea_market_app.listing.domain;

import java.util.Objects;
import java.util.UUID;

import com.example.flea_market_app.catalog.domain.ItemEntity;

import lombok.Getter;

/**
 * 出品まわりのドメインモデル（ItemEntity と 1:1 対応）。
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

	/** 全フィールドを受け取るコンストラクタ（fromEntity 用）。 */
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

	/** Entity からドメインを組み立てる。 */
	public static Listing fromEntity(ItemEntity e) {
		Objects.requireNonNull(e);
		return new Listing(
				e.getId(),
				e.getCopiedFromItemId(),
				e.getCopySource(),
				e.getSellerId(),
				e.getCategoryId(),
				e.getName(),
				e.getDescription(),
				e.getPriceAmount() != null ? e.getPriceAmount() : 0L,
				e.getCurrency() != null ? e.getCurrency() : "JPY",
				ItemStatus.valueOf(e.getStatus() != null ? e.getStatus() : "DRAFT"),
				ItemCondition.valueOf(e.getCondition()),
				ShippingFeePayer.valueOf(e.getShippingFeePayer()));
	}

	/** ItemEntity を組み立てる。createdAt/updatedAt は Entity の @PrePersist に任せる。 */
	public ItemEntity toEntity() {
		ItemEntity entity = new ItemEntity();
		entity.setId(id);
		entity.setCopiedFromItemId(copiedFromItemId);
		entity.setCopySource(copySource);
		entity.setSellerId(sellerId);
		entity.setCategoryId(categoryId);
		entity.setName(name);
		entity.setDescription(description);
		entity.setPriceAmount(priceAmount);
		entity.setCurrency(currency);
		entity.setStatus(status.name());
		entity.setCondition(condition.name());
		entity.setShippingFeePayer(shippingFeePayer.name());
		// createdAt, updatedAt は null のまま（@PrePersist / @PreUpdate で設定）
		return entity;
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
