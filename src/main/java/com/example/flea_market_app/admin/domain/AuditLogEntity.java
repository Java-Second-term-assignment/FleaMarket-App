package com.example.flea_market_app.admin.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * DB: audit_logs
 *
 * 管理者操作の「証跡」。
 * - 何をしたか(action)
 * - 何に対してか(target_type + target_id)
 * - なぜか(reason)
 *
 * ※ admin_user_id は auth_users.id を参照している点に注意（users.id ではない）
 */

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLogEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "admin_user_id", nullable = false, columnDefinition = "uuid")
	private UUID adminUserId; // auth_users.id

	@Column(nullable = false)
	private String action;

	@Column(name = "target_type", nullable = false)
	private String targetType;

	@Column(name = "target_id", nullable = false, columnDefinition = "uuid")
	private UUID targetId;

	@Column(nullable = false, length = 2000)
	private String reason;

	@Column(name = "occurred_at", nullable = false)
	private OffsetDateTime occurredAt;

	// リクエスト相関用（将来の運用のために配置）
	@Column(name = "request_id")
	private String requestId;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "user_agent")
	private String userAgent;

}
