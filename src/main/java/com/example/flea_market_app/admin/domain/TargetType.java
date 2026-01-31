package com.example.flea_market_app.admin.domain;

/**
 * DB CHECK と一致させる
 * reports.report_type IN (...)
 */

public enum TargetType {
	ITEM, USER, ORDER, CHAT_MESSAGE
}