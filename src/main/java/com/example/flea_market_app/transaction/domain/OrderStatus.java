package com.example.flea_market_app.transaction.domain;

/**
 * DB: orders.status CHECK
 * 'PAID','AWAITING_SHIPMENT','SHIPPED','COMPLETED','CANCELLED'
 */

public enum OrderStatus {

	PAID, AWAITING_SHIPMENT, SHIPPED, COMPLETED, CANCELLED

}
