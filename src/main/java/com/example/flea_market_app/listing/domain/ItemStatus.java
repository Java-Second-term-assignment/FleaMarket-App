package com.example.flea_market_app.listing.domain;

/**
 * 商品の出品状態（DB の status と一致）。
 */
public enum ItemStatus {
	DRAFT,
	PUBLISHED,
	IN_TRADE,
	SOLD,
	SUSPENDED,
	DELETED
}
