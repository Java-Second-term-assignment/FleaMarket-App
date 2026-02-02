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

		// 2) principal が UserDetails 実装で、username に userId(UUID文字列)を入れてるケース
		if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
			return UUID.fromString(ud.getUsername());
		}

		// 3) principal が String のケース（userId文字列）
		if (auth.getPrincipal() instanceof String s) {
			return UUID.fromString(s);
		}

		throw new IllegalStateException("Unsupported principal type: " + auth.getPrincipal().getClass().getName());
	}
}
