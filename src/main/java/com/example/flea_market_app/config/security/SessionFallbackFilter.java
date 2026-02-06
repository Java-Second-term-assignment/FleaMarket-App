package com.example.flea_market_app.config.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * API チェーン内で /api/user/** へのリクエストについて、
 * JWT で認証されていない場合にセッションから認証を復元する。
 *
 * <p>同一オリジン Thymeleaf 上の fetch（credentials: "same-origin"）が
 * Cookie のみで /api/user/me 等を呼ぶケースをサポートする。
 * 新規セッションは作成しない（getSession(false)）。
 */
@Component
public class SessionFallbackFilter extends OncePerRequestFilter {

	private static final String API_USER_PATH_PREFIX = "/api/user/";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {

		// /api/user/** 以外は何もしない
		String path = request.getServletPath();
		if (path == null || !path.startsWith(API_USER_PATH_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		// 既に認証済みなら何もしない（JWT で認証済みのケース）
		Authentication current = SecurityContextHolder.getContext().getAuthentication();
		if (current != null && current.isAuthenticated()
				&& !(current instanceof AnonymousAuthenticationToken)) {
			filterChain.doFilter(request, response);
			return;
		}

		// 既存セッションから認証を復元（新規セッションは作成しない）
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object ctxObj = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
			if (ctxObj instanceof SecurityContext ctx
					&& ctx.getAuthentication() != null
					&& ctx.getAuthentication().isAuthenticated()
					&& !(ctx.getAuthentication() instanceof AnonymousAuthenticationToken)) {
				SecurityContextHolder.getContext().setAuthentication(ctx.getAuthentication());
			}
		}

		filterChain.doFilter(request, response);
	}
}
