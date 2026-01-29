package com.example.flea_market_app.engagement.notification.service;

import java.util.UUID;

/**
 * 通知メールを送信する窓口。
 * 送信先は AuthUser のメールアドレスで解決する。
 */
public interface EmailNotificationSender {

	/**
	 * 取引成立時：売り手にメールを送る。
	 *
	 * @param sellerUserId 売り手のユーザーID
	 * @param orderId      注文ID
	 */
	void sendTransactionEstablished(UUID sellerUserId, UUID orderId);

	/**
	 * チャット受信時：受信者にメールを送る。
	 *
	 * @param recipientUserId 受信者のユーザーID
	 * @param orderId         注文ID
	 */
	void sendChatReceived(UUID recipientUserId, UUID orderId);
}
