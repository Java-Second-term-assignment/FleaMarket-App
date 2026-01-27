package com.example.flea_market_app.user.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * ユーザーエンティティ。
 * 
 * <p>フリマアプリケーションのユーザー情報を表すエンティティです。
 * プロフィール画像のS3キーも保持します。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

	/** ユーザーID（UUID） */
	@Id
	@Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	/** 表示名 */
	@Column(name = "display_name", nullable = false)
	private String displayName;

	/** ユーザーランクID */
	@Column(name = "user_rank_id", nullable = false)
	private Short userRankId;

	/** 本人確認ステータス */
	@Column(name = "identity_status", nullable = false)
	private String identityStatus;

	/** アクティブフラグ */
	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	/** プロフィール画像のS3キー */
	@Column(name = "profile_image_s3_key")
	private String profileImageS3Key;

	/** 作成日時 */
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	/** 更新日時 */
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

}
