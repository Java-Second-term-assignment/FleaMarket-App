package com.example.flea_market_app.auth.service;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * フォームログイン用の UserDetails。username に加えて userId (UUID) を保持し、
 * SecurityUtil で確実にユーザーIDを取得できるようにする。
 */
public class UserIdUserDetails extends User {

	private final UUID userId;

	public UserIdUserDetails(UUID userId, String password, Collection<? extends GrantedAuthority> authorities) {
		super(userId.toString(), password, authorities);
		this.userId = userId;
	}

	public UUID getUserId() {
		return userId;
	}
}
