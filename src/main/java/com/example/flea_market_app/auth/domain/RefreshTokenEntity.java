package com.example.flea_market_app.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DBの要件と合致させること!!
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshTokenEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "user_id", columnDefinition = "uuid", nullable = false)
	private UUID userId;

	@Column(name = "token_hash", nullable = false, unique = true)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	@Column(name = "revoked_at")
	private OffsetDateTime revokedAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	public boolean isExpired(OffsetDateTime now) {
		return now.isAfter(expiresAt);
	}

	public boolean isRevoked() {
		return revokedAt != null;
	}

}
