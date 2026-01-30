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
 * DB: reports
 *
 * 通報情報。
 * - target_type / target_id により横断で通報できる
 * - report_type は限定 enum（DB CHECK）
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
public class ReportEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "reporter_id", nullable = false, columnDefinition = "uuid")
	private UUID reporterId; // users.id

	@Column(name = "target_type", nullable = false)
	private String targetType;

	@Column(name = "target_id", nullable = false, columnDefinition = "uuid")
	private UUID targetId;

	@Column(name = "report_type", nullable = false)
	private String reportType;

	@Column(length = 2000)
	private String description;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
