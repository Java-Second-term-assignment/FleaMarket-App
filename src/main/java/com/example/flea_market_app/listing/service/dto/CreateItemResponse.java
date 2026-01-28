package com.example.flea_market_app.listing.service.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 商品作成レスポンスDTO。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class CreateItemResponse {

	/** 商品ID */
	private UUID itemId;

	/** 画像URLリスト（表示順） */
	private List<String> imageUrls;
}
