package com.example.flea_market_app.auth.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.domain.PasswordResetTokenEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.auth.repository.PasswordResetTokenRepository;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.common.validation.PasswordPolicy;
import com.example.flea_market_app.engagement.notification.service.EmailNotificationSender;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private static final SecureRandom RNG = new SecureRandom();
	private static final int TOKEN_BYTES = 32;

	private final AuthUserRepository authUserRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenHasher tokenHasher;
	private final EmailNotificationSender emailNotificationSender;

	@Value("${app.base-url:http://localhost:8080}")
	private String baseUrl;

	@Value("${app.password-reset.token-expiration-minutes:60}")
	private int tokenExpirationMinutes;

	/**
	 * パスワード再設定をリクエストする。メールが未登録でも同じ成功メッセージを返す（情報漏洩防止）。
	 */
	@Transactional
	public void requestReset(String email) {
		if (email == null || email.isBlank()) {
			return;
		}
		Optional<AuthUserEntity> authOpt = authUserRepository.findByEmail(email.trim());
		if (authOpt.isEmpty()) {
			return;
		}
		AuthUserEntity authUser = authOpt.get();
		UUID userId = authUser.getUserId();

		// 同一ユーザーの既存トークンは削除（1ユーザー1有効トークン）
		passwordResetTokenRepository.deleteByUserId(userId);

		String rawToken = generateRawToken();
		String tokenHash = tokenHasher.hash(rawToken);
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime expiresAt = now.plusMinutes(tokenExpirationMinutes);

		PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
		entity.setId(UUID.randomUUID());
		entity.setTokenHash(tokenHash);
		entity.setUserId(userId);
		entity.setExpiresAt(expiresAt);
		entity.setCreatedAt(now);
		passwordResetTokenRepository.save(entity);

		String resetLink = baseUrl + "/password/reset?token=" + rawToken;
		emailNotificationSender.sendPasswordResetLink(authUser.getEmail(), resetLink);
	}

	/**
	 * トークンを使ってパスワードを再設定する。トークン無効・期限切れの場合は例外。
	 */
	@Transactional
	public void resetPassword(String rawToken, String newPassword) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_PASSWORD,
					"error.password_reset.invalid_token");
		}
		PasswordPolicy.validate(newPassword);

		String tokenHash = tokenHasher.hash(rawToken);
		PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new ValidationBusinessException(
						com.example.flea_market_app.common.error.ErrorCode.INVALID_PASSWORD,
						"error.password_reset.invalid_token"));

		if (tokenEntity.isExpired(OffsetDateTime.now())) {
			passwordResetTokenRepository.delete(tokenEntity);
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_PASSWORD,
					"error.password_reset.invalid_token");
		}

		UUID userId = tokenEntity.getUserId();
		String encodedPassword = passwordEncoder.encode(newPassword);
		authUserRepository.updatePasswordHashByUserId(userId, encodedPassword);
		passwordResetTokenRepository.delete(tokenEntity);
	}

	private static String generateRawToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		RNG.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
