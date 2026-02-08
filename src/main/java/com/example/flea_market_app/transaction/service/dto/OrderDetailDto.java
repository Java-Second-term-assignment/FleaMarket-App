package com.example.flea_market_app.transaction.service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注文詳細表示用DTO。一覧用の情報に加え、配送先・相手表示名・操作可否フラグを持つ。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {

	private UUID orderId;
	private UUID itemId;
	private String itemName;
	private String itemThumbnailUrl;
	private String status;
	private Long totalAmount;
	private OffsetDateTime createdAt;
	private String role;

	private String shippingAddressSnapshot;
	/** お届け先の表示用整形文字列（名前・郵便番号・住所） */
	private String shippingAddressFormatted;
	private String counterpartyDisplayName;

	private boolean canConfirm;
	private boolean canShip;
	private boolean canReceipt;
	private boolean canCancel;
	private boolean canReview;
}
