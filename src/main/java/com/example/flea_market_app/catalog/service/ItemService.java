package com.example.flea_market_app.catalog.service;

import java.util.UUID;

/**
 * 商品の作成を行うサービスのインターフェース。
 */
public interface ItemService {

	/**
	 * 下書き商品（status=DRAFT）を作成する。
	 *
	 * @param sellerId        出品者ユーザーID
	 * @param categoryId      カテゴリID
	 * @param name            商品名
	 * @param description     説明
	 * @param priceAmount     価格（最小通貨単位）
	 * @param condition       商品状態（NEW, LIKE_NEW, USED_GOOD, USED_FAIR, USED_POOR）
	 * @param shippingFeePayer 配送料負担者（SELLER, BUYER）
	 * @return 作成された商品のID
	 */
	UUID createDraftItem(UUID sellerId, UUID categoryId, String name, String description,
			long priceAmount, String condition, String shippingFeePayer);
}
