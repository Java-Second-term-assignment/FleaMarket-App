package com.example.flea_market_app.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.common.validation.PasswordPolicy;

import lombok.RequiredArgsConstructor;

/**
 * ログイン中のユーザーが自分のパスワードを変更するためのサービス。
 */
@Service
@RequiredArgsConstructor
public class PasswordChangeService {

	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 指定ユーザーのパスワードを更新する。
	 *
	 * @param userId     ログイン中ユーザーのID（users.id = auth_users.user_id）
	 * @param newPassword 新しいパスワード（平文）。PasswordPolicy で検証される。
	 */
	@Transactional
	public void changePassword(UUID userId, String newPassword) {
		PasswordPolicy.validate(newPassword);
		String encoded = passwordEncoder.encode(newPassword);
		authUserRepository.updatePasswordHashByUserId(userId, encoded);
	}
}
