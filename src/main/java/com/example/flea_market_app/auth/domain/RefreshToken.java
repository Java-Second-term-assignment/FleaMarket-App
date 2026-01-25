package com.example.flea_market_app.auth.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

	@Id
	private String token;

	private String userId;
	private LocalDateTime expiresAt;

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}

}
