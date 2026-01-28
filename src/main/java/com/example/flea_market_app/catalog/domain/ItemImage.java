package com.example.flea_market_app.catalog.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_images")
@Getter
@Setter
public class ItemImage {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "item_id", nullable = false)
	private UUID itemId;

	@Column(name = "s3_key", nullable = false)
	private String s3Key;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "byte_size")
	private Long byteSize;

	@Column(name = "display_order", nullable = false)
	private Short displayOrder;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}
}
