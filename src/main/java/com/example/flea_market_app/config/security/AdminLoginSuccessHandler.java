package com.example.flea_market_app.config.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理者ログイン専用の成功ハンドラ。
 * 認証後に ROLE_ADMIN を持っていない場合はセッションを破棄し、
 * 管理者ログインページへエラー付きでリダイレクトする。
 */
@Slf4j
public class AdminLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final String ADMIN_ROLE = "ROLE_ADMIN";
	private static final String FORBIDDEN_PARAM = "?error=forbidden";

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		Collection<? extends GrantedAuthority> authorities =
				authentication.getAuthorities() != null
						? authentication.getAuthorities()
						: AuthorityUtils.NO_AUTHORITIES;

		boolean hasAdmin = authorities.stream()
				.anyMatch(a -> ADMIN_ROLE.equals(a.getAuthority()));

		if (!hasAdmin) {
			log.warn("Non-admin user attempted admin login: {}", authentication.getName());
			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			getRedirectStrategy().sendRedirect(request, response, "/admin/login" + FORBIDDEN_PARAM);
			return;
		}

		super.onAuthenticationSuccess(request, response, authentication);
	}
}
