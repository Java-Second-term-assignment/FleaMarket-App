package com.example.flea_market_app.engagement.favorite.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "favorites")
@Getter
@Setter
public class FavoriteEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "item_id", nullable = false)
	private UUID itemId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
