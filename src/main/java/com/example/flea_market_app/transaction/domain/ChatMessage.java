package com.example.flea_market_app.transaction.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

/**
 * 取引内メッセージ（ドメイン）
 *
 * 役目:
 * - content の前処理（trim等）とルール検証（空文字禁止、最大4000文字）
 * - 将来: テンプレ判定/NGワード/通知イベントの条件などを集約できる
 *
 * 注意:
 * - 永続の実体は order_messages（OrderMessageEntity）
 * - 監査のため編集不可前提（基本 immutable で扱う）
 */
public class ChatMessage {

	private final UUID id;
	private final UUID orderId;
	private final UUID senderId;
	private final String content;
	private final boolean template;
	private final OffsetDateTime createdAt;

	private ChatMessage(
			UUID id,
			UUID orderId,
			UUID senderId,
			String content,
			boolean template,
			OffsetDateTime createdAt) {
		this.id = Objects.requireNonNull(id);
		this.orderId = Objects.requireNonNull(orderId);
		this.senderId = Objects.requireNonNull(senderId);
		this.content = normalizeAndValidate(content);
		this.template = template;
		this.createdAt = Objects.requireNonNull(createdAt);
	}

	/** 通常のユーザー投稿 */
	public static ChatMessage user(UUID orderId, UUID senderId, String content, OffsetDateTime now) {
		Objects.requireNonNull(now);
		return new ChatMessage(UUID.randomUUID(), orderId, senderId, content, false, now);
	}

	/** テンプレ投稿（将来用） */
	public static ChatMessage template(UUID orderId, UUID senderId, String content, OffsetDateTime now) {
		Objects.requireNonNull(now);
		return new ChatMessage(UUID.randomUUID(), orderId, senderId, content, true, now);
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public UUID getSenderId() {
		return senderId;
	}

	public String getContent() {
		return content;
	}

	public boolean isTemplate() {
		return template;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	private static String normalizeAndValidate(String content) {
		if (content == null) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.chat_message.required");
		}

		String trimmed = content.trim();

		if (trimmed.isEmpty()) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.chat_message.empty");

		}

		if (trimmed.length() > 4000) {
			throw new ValidationBusinessException(ErrorCode.INVALID_STATE, "error.chat_message.too_long");
		}

		return trimmed;
	}
}
