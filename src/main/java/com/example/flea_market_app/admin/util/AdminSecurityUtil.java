package com.example.flea_market_app.admin.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.admin.service.port.AdminAuthQueryPort;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;

import lombok.RequiredArgsConstructor;

/**
 * 管理者アクセス制御の共通化。
 *
 * 注意:
 * - システム内の「ログインユーザーID」は users.id を使うことが多い
 * - しかし audit_logs.admin_user_id は auth_users.id を参照する
 *   → admin操作では auth_users.id も必要になるためここで解決する
 */
@Component
@RequiredArgsConstructor
public class AdminSecurityUtil {

	private final AdminAuthQueryPort adminAuthQueryPort;

	public UUID requireAdminAndGetAuthUserId(UUID currentUserId) {
		AdminAuthQueryPort.AdminAuthInfo info = adminAuthQueryPort.findByUserId(currentUserId)
				.orElseThrow(() -> NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND));

		if (!info.isAdmin()) {
			throw new AccessDeniedBusinessException();
		}
		return info.getAuthUserId();
	}
}
