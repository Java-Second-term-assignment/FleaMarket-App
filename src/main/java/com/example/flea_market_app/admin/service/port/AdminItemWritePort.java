package com.example.flea_market_app.admin.service.port;

import java.util.UUID;

/**
 * AdminServiceが「itemsへの書き込み」をどこに委譲するかを隠すポート。
 * - 将来: ListingService へ差し替え
 * - いま: 直SQL / JPA更新でOK
 */
public interface AdminItemWritePort {
	boolean markDeleted(UUID itemId);

	boolean restoreFromDeleted(UUID itemId);

	boolean deletePermanently(UUID itemId);
}
