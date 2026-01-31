package com.example.flea_market_app.admin.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.domain.AuditLogEntity;
import com.example.flea_market_app.admin.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * 管理者操作の監査ログを一元管理する。
 *
 * 原則:
 * - admin系の更新操作は「必ず」ここを通す（漏れを防ぐ）
 * - reason は必須（誰が見ても後で説明できる状態にする）
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	@Transactional
	public void record(UUID adminAuthUserId, String action, String targetType, UUID targetId, String reason) {
		AuditLogEntity e = new AuditLogEntity();
		e.setId(UUID.randomUUID());
		e.setAdminUserId(adminAuthUserId);
		e.setAction(action);
		e.setTargetType(targetType);
		e.setTargetId(targetId);
		e.setReason(reason);
		e.setOccurredAt(OffsetDateTime.now());

		auditLogRepository.save(e);
	}
}
