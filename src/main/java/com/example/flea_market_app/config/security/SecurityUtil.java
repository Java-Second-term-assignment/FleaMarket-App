package com.example.flea_market_app.config.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

	private SecurityUtil() {
	}

	public static Optional<UUID> getCurrentUserIdOptional() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null || !auth.isAuthenticated()) {
			return Optional.empty();
		}
		if (auth.getPrincipal() instanceof UUID uuid) {
			return Optional.of(uuid);
		}
		if (auth.getPrincipal() instanceof com.example.flea_market_app.auth.service.UserIdUserDetails ud) {
			return Optional.of(ud.getUserId());
		}
		if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
			return Optional.of(UUID.fromString(ud.getUsername()));
		}
		if (auth.getPrincipal() instanceof String s) {
			try {
				return Optional.of(UUID.fromString(s));
			} catch (IllegalArgumentException e) {
				// 未ログイン時は principal が "anonymousUser" になるため UUID として解釈できない
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	public static UUID getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			throw new IllegalStateException("Unauthenticated");
		}

		// 1) principal が UUID を直接持つケース
		if (auth.getPrincipal() instanceof UUID uuid) {
			return uuid;
		}

		// 2) principal が UserIdUserDetails のケース（フォームログインで userId を保持）
		if (auth.getPrincipal() instanceof com.example.flea_market_app.auth.service.UserIdUserDetails ud) {
			return ud.getUserId();
		}
		// 3) principal が UserDetails で username が UUID 文字列のケース
		if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
			String username = ud.getUsername();
			if (username == null || username.isBlank()) {
				throw new IllegalStateException("Unauthenticated: username is null or empty");
			}
			return UUID.fromString(username);
		}

		// 4) principal が String のケース（userId文字列）
		if (auth.getPrincipal() instanceof String s) {
			return UUID.fromString(s);
		}

		throw new IllegalStateException("Unsupported principal type: " + auth.getPrincipal().getClass().getName());
	}
}
