package com.example.flea_market_app.admin.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * DB: item_moderation
 * 出品時AIモデレーションで「違反」と判定された結果を保存する。
 */
@Entity
@Table(name = "item_moderation")
@Getter
@Setter
public class ItemModerationEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "item_id", nullable = false)
	private UUID itemId;

	@Column(name = "rejected", nullable = false)
	private boolean rejected;

	@Column(name = "text_flagged")
	private Boolean textFlagged;

	@Column(name = "text_negative_score")
	private Double textNegativeScore;

	@Column(name = "image_adult")
	private Boolean imageAdult;

	@Column(name = "image_violence")
	private Boolean imageViolence;

	@Column(name = "image_risk_score")
	private Double imageRiskScore;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}
}
