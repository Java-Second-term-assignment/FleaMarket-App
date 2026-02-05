package com.example.flea_market_app.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtSupport {

	private final SecretKey secretKey;
	private final java.time.Duration accessTokenExpiration;

	public JwtSupport(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-expiration}") String accessTokenExpiration) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpiration = DurationStyle.SIMPLE.parse(accessTokenExpiration);
	}

	/**
	 * アクセストークン（JWT）を発行する。クレーム: sub=userId, iat, exp。アルゴリズム: HS256。
	 */
	public String createAccessToken(UUID userId) {
		Instant now = Instant.now();
		Instant exp = now.plus(accessTokenExpiration);
		return Jwts.builder()
				.subject(userId.toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(secretKey)
				.compact();
	}

	/**
	 * アクセストークン（JWT）を検証し、sub クレームから userId を返す。
	 * 署名不正・期限切れ・パース失敗の場合は Optional.empty()。
	 */
	public Optional<UUID> parseUserIdFromAccessToken(String token) {
		try {
			Claims payload = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			String sub = payload.getSubject();
			if (sub == null || sub.isBlank()) {
				return Optional.empty();
			}
			return Optional.of(UUID.fromString(sub));
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
