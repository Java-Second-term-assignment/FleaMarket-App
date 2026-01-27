package com.example.flea_market_app.user.controller;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

final class SecurityUtil {

	private SecurityUtil() {
	}

	public static UUID getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			throw new IllegalStateException("Unauthenticated");
		}

		// 例：principalに userId(UUID) を入れている想定
		// 実プロジェクトに合わせてここを調整
		if (auth.getPrincipal() instanceof String s) {
			return UUID.fromString(s);
		}

		throw new IllegalStateException("Unsupported principal type: " + auth.getPrincipal().getClass());
	}
}
