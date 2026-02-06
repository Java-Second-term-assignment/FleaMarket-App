package com.example.flea_market_app.transaction.service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.flea_market_app.transaction.domain.OrderMessageEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 契約用のチャットメッセージレスポンス DTO。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

	private UUID id;
	private UUID orderId;
	private UUID senderId;
	private String content;
	private boolean template;
	private OffsetDateTime createdAt;

	public static ChatMessageResponse from(OrderMessageEntity e) {
		return new ChatMessageResponse(
				e.getId(),
				e.getOrderId(),
				e.getSenderId(),
				e.getContent(),
				e.isTemplate(),
				e.getCreatedAt());
	}
}
