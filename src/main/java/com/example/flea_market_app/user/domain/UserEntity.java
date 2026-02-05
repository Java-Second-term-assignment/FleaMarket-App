package com.example.flea_market_app.user.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "user_rank_id", nullable = false)
	private short userRankId;

	@Column(name = "identity_status", nullable = false)
	private String identityStatus; // UNVERIFIED etc

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "frozen_until")
	private OffsetDateTime frozenUntil;

	@Column(name = "profile_image_s3_key")
	private String profileImageS3Key;

	@Column(name = "caption", length = 200)
	private String caption;
}
