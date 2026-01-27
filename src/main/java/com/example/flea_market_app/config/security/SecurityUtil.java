package com.example.flea_market_app.config.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

	private SecurityUtil() {
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
