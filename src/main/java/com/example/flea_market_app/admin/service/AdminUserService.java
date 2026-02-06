package com.example.flea_market_app.admin.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.domain.TargetType;
import com.example.flea_market_app.admin.service.port.AdminUserWritePort;
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
	public void freezeUser(UUID currentUserId, UUID targetUserId, String reason, OffsetDateTime frozenUntil) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminUserWritePort.setActiveAndFrozenUntil(targetUserId, false, frozenUntil);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "FREEZE_USER", TargetType.USER.name(), targetUserId, reason);
	}

	@Transactional
	public void forceWithdraw(UUID currentUserId, UUID targetUserId, String reason) {
		// MVPでは freeze と同義（無期限凍結）
		freezeUser(currentUserId, targetUserId, reason, null);
	}

	@Transactional
	public void toggleUserActive(UUID currentUserId, UUID targetUserId) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);
		boolean updated = adminUserWritePort.toggleActive(targetUserId);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);
		auditLogService.record(adminAuthUserId, "TOGGLE_USER_ACTIVE", TargetType.USER.name(), targetUserId, null);
	}

	@Transactional
	public void restoreUser(UUID currentUserId, UUID targetUserId) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminUserWritePort.setActiveAndFrozenUntil(targetUserId, true, null);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "RESTORE_USER", TargetType.USER.name(), targetUserId, "制限解除");
	}

	@Transactional
	public void deleteUserPermanently(UUID currentUserId, UUID targetUserId) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean deleted = adminUserWritePort.deleteUserPermanently(targetUserId);
		if (!deleted)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "DELETE_USER_PERMANENTLY", TargetType.USER.name(), targetUserId, "永久削除");
	}

	@Transactional
	public void updateUserProfile(UUID currentUserId, UUID targetUserId, String displayName, String email) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminUserWritePort.updateUserProfile(targetUserId, displayName, email);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "UPDATE_USER_PROFILE", TargetType.USER.name(), targetUserId, null);
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
}
