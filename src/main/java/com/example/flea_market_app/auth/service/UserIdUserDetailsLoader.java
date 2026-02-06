package com.example.flea_market_app.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * userId から UserIdUserDetails を組み立てる。JWT 認証フィルタで principal を設定する際に使用する。
 */
@Component
@RequiredArgsConstructor
public class UserIdUserDetailsLoader {

	private final AuthUserRepository authUserRepository;

	public Optional<UserIdUserDetails> loadByUserId(UUID userId) {
		return authUserRepository.findByUserId(userId)
				.map(this::toUserIdUserDetails);
	}

	private UserIdUserDetails toUserIdUserDetails(AuthUserEntity entity) {
		List<GrantedAuthority> authorities = new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_USER")));
		if (entity.isAdmin()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		// JWT 認証ではパスワードを検証しないため空文字でよい
		return new UserIdUserDetails(entity.getUserId(), "", authorities);
	}
}
