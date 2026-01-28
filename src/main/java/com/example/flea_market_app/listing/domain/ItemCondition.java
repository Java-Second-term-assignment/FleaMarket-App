package com.example.flea_market_app.listing.domain;

/**
 * 商品の状態（DB の condition と一致）。
 */
public enum ItemCondition {
	NEW,
	LIKE_NEW,
	USED_GOOD,
	USED_FAIR,
	USED_POOR
}
