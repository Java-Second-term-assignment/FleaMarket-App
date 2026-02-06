package com.example.flea_market_app.engagement.notification.service;

import java.util.List;
import java.util.UUID;

import com.example.flea_market_app.engagement.notification.domain.NotificationEntity;

public interface NotificationService {

	/**
	 * ユーザーに通知を1件作成する。
	 */
	void create(UUID userId, String message);

	/**
	 * ユーザーの通知一覧を新しい順で取得する（最大 limit 件）。
	 */
	List<NotificationEntity> listByUser(UUID userId, int limit);
}
