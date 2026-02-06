package com.example.flea_market_app.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "token_hash", nullable = false, unique = true)
	private String tokenHash;

	@Column(name = "user_id", columnDefinition = "uuid", nullable = false)
	private UUID userId;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	public boolean isExpired(OffsetDateTime now) {
		return now.isAfter(expiresAt);
	}
}
