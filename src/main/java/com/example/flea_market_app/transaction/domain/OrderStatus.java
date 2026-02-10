package com.example.flea_market_app.transaction.domain;

/**
 * DB: orders.status CHECK
 * 'PENDING','PAID','AWAITING_SHIPMENT','SHIPPED','COMPLETED','CANCELLED'
 */

public enum OrderStatus {

	PENDING, PAID, AWAITING_SHIPMENT, SHIPPED, COMPLETED, CANCELLED;

	/**
	 * 文字列を OrderStatus に変換する。null/空文字または不正な enum 名の場合は IllegalStateException。
	 */
	public static OrderStatus fromString(String statusStr) {
		if (statusStr == null || statusStr.isBlank()) {
			throw new IllegalStateException("Order status is null or blank");
		}
		try {
			return valueOf(statusStr);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Order status is invalid: " + statusStr, e);
		}
	}

}
