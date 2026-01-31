package com.example.flea_market_app.admin.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.domain.TargetType;
import com.example.flea_market_app.admin.util.AdminSecurityUtil;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;

import lombok.RequiredArgsConstructor;

/**
 * ユーザーに関する管理者操作。
 * users テーブルは is_active を持つので、凍結= false が自然
 * 方針:
 * - 「削除」はDB DELETEではなく状態遷移（is_active=false）
 * - 権限変更は auth_users.is_admin を更新（最小）
 * - 必ず audit を残す
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final AdminSecurityUtil adminSecurityUtil;
	private final AuditLogService auditLogService;

	private final AdminUserWritePort adminUserWritePort;

	@Transactional
	public void freezeUser(UUID currentUserId, UUID targetUserId, String reason) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminUserWritePort.setActive(targetUserId, false);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "FREEZE_USER", TargetType.USER.name(), targetUserId, reason);
	}

	@Transactional
	public void forceWithdraw(UUID currentUserId, UUID targetUserId, String reason) {
		// MVPでは freeze と同義（将来: 退会理由、退会状態などを追加）
		freezeUser(currentUserId, targetUserId, reason);
	}

	@Transactional
	public void changeAdminRole(UUID currentUserId, UUID targetUserId, boolean makeAdmin, String reason) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminUserWritePort.setAdminByUserId(targetUserId, makeAdmin);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(
				adminAuthUserId,
				makeAdmin ? "GRANT_ADMIN" : "REVOKE_ADMIN",
				TargetType.USER.name(),
				targetUserId,
				reason);
	}

	public interface AdminUserWritePort {
		boolean setActive(UUID userId, boolean active);

		boolean setAdminByUserId(UUID userId, boolean admin);
	}
}
