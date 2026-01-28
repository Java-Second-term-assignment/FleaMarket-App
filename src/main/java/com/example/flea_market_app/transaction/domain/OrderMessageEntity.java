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
 * DB: order_messages
 *
 * 注意:
 * - content <= 4000（DB CHECK）
 * - 編集不可（監査のため）
 */
@Entity
@Table(name = "order_messages")
@Getter
@Setter
public class OrderMessageEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "order_id", nullable = false, columnDefinition = "uuid")
	private UUID orderId;

	@Column(name = "sender_id", nullable = false, columnDefinition = "uuid")
	private UUID senderId;

	@Column(nullable = false, length = 4000)
	private String content;

	@Column(name = "is_template", nullable = false)
	private boolean isTemplate;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
