package com.example.flea_market_app.admin.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.domain.TargetType;
import com.example.flea_market_app.admin.service.port.AdminItemWritePort;
import com.example.flea_market_app.admin.util.AdminSecurityUtil;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;

import lombok.RequiredArgsConstructor;

/**
 * 出品物に関する管理者操作。
 * DB上、items は status が DELETED などを持つので、
 * 物理削除ではなく status を DELETED にする。
 *
 * 責務:
 * - 管理者操作を正規ドメイン（Listing/Catalog/Transaction）へ委譲するのが理想
 * - MVPでは最低限の「強制削除（論理削除）」を提供
 * - 必ず Audit を残す
 */
@Service
@RequiredArgsConstructor
public class AdminService {

	private final AdminSecurityUtil adminSecurityUtil;
	private final AuditLogService auditLogService;

	// MVPで listing ドメイン未整備の場合は、items を直接更新するRepositoryを別途用意する
	private final AdminItemWritePort adminItemWritePort;

	@Transactional
	public void forceDeleteItem(UUID currentUserId, UUID itemId, String reason) {
		// currentUserId = users.id を想定
		// audit_logs.admin_user_id は auth_users.id を要求するので変換が必要
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminItemWritePort.markDeleted(itemId);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(
				adminAuthUserId,
				"FORCE_DELETE_ITEM",
				TargetType.ITEM.name(),
				itemId,
				reason);
	}

	@Transactional
	public void restoreItem(UUID currentUserId, UUID itemId) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean updated = adminItemWritePort.restoreFromDeleted(itemId);
		if (!updated)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "RESTORE_ITEM", TargetType.ITEM.name(), itemId, "復元");
	}

	@Transactional
	public void deleteItemPermanently(UUID currentUserId, UUID itemId) {
		UUID adminAuthUserId = adminSecurityUtil.requireAdminAndGetAuthUserId(currentUserId);

		boolean deleted = adminItemWritePort.deletePermanently(itemId);
		if (!deleted)
			throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);

		auditLogService.record(adminAuthUserId, "DELETE_ITEM_PERMANENTLY", TargetType.ITEM.name(), itemId, "永久削除");
	}

}
