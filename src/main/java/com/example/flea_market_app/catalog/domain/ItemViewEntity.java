package com.example.flea_market_app.catalog.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_views", uniqueConstraints = {
		@UniqueConstraint(name = "uq_item_views_user_item", columnNames = { "user_id", "item_id" })
})
@Getter
@Setter
public class ItemViewEntity {

	@Id
	@Column(name = "id", nullable = false, columnDefinition = "uuid")
	private UUID id;

	@Column(name = "user_id", nullable = false, columnDefinition = "uuid")
	private UUID userId;

	@Column(name = "item_id", nullable = false, columnDefinition = "uuid")
	private UUID itemId;

	@Column(name = "viewed_at", nullable = false)
	private OffsetDateTime viewedAt;
}
