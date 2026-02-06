package com.example.flea_market_app.admin.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * グラフ用：閲覧数トップ商品。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopViewedItemDto {

	private UUID itemId;
	private String itemName;
	private long viewCount;
}
