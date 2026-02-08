package com.example.flea_market_app.user.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** ユーザー設定画面「出品した商品」タブ用の1件表示DTO。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProductItemDto {

	private UUID id;
	private String imageUrl;
	private String name;
	/** この商品に紐づく注文ID。取引があるときのみセット。 */
	private UUID orderId;
}
