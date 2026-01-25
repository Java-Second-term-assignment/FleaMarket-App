package com.example.flea_market_app.auth.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.auth.domain.RefreshTokenEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final TokenHasher tokenHasher;

	public String generateAccessToken(UUID userId) {
		// JWT生成(簡略)
		return "access-" + UUID.randomUUID();
	}

	public IssuedRefreshToken issueRefreshToken(UUID userId) {
		String rawToken = UUID.randomUUID().toString(); // クライアントへ返す
		String tokenHash = tokenHasher.hash(rawToken); // DBへ保存する

		OffsetDateTime now = OffsetDateTime.now();
		RefreshTokenEntity entity = new RefreshTokenEntity(
				UUID.randomUUID(),
				userId,
				tokenHash,
				now.plusDays(30),
				null,
				now);

		return new IssuedRefreshToken(rawToken, entity);
	}

	// 内部返却用（recordでもOK）
	public static class IssuedRefreshToken {
		private final String rawToken;
		private final RefreshTokenEntity entity;

		public IssuedRefreshToken(String rawToken, RefreshTokenEntity entity) {
			this.rawToken = rawToken;
			this.entity = entity;
		}

		public String getRawToken() {
			return rawToken;
		}

		public RefreshTokenEntity getEntity() {
			return entity;
		}
	}
}
