package com.example.flea_market_app.transaction.service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注文一覧表示用DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemDto {

	private UUID orderId;
	private UUID itemId;
	private String itemName;
	private String itemThumbnailUrl;
	private String status;
	private Long totalAmount;
	private OffsetDateTime createdAt;
	/** 現在ユーザーが買い手なら "BUYER", 売り手なら "SELLER" */
	private String role;
}
