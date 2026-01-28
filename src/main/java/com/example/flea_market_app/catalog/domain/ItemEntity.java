package com.example.flea_market_app.catalog.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
public class ItemEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "copied_from_item_id")
	private UUID copiedFromItemId;

	@Column(name = "copy_source")
	private String copySource;

	@Column(name = "seller_id", nullable = false)
	private UUID sellerId;

	@Column(name = "category_id", nullable = false)
	private UUID categoryId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", nullable = false, columnDefinition = "text")
	private String description;

	@Column(name = "price_amount", nullable = false)
	private Long priceAmount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "condition", nullable = false)
	private String condition;

	@Column(name = "shipping_fee_payer", nullable = false)
	private String shippingFeePayer;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (status == null) {
			status = "DRAFT";
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = OffsetDateTime.now();
	}
}
