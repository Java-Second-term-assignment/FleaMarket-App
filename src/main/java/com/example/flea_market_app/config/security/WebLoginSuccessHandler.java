package com.example.flea_market_app.config.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
 * 一般ユーザー用ログイン成功ハンドラ。
 * 管理者（ROLE_ADMIN）が一般ログインした場合は認証を完了させず、管理者ログインページへリダイレクトする。
 * リクエストに returnUrl パラメータがあればそこへリダイレクト（オープンリダイレクト対策済み）。
 * なければデフォルトの /products へ。
 */
@Slf4j
public class WebLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final String ADMIN_ROLE = "ROLE_ADMIN";
	private static final String PARAM_RETURN_URL = "returnUrl";
	private static final String DEFAULT_TARGET = "/products";

	public WebLoginSuccessHandler() {
		super();
		setDefaultTargetUrl(DEFAULT_TARGET);
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		// 管理者は一般ログインからは入れない。管理者ログインページへ誘導する。
		Collection<? extends GrantedAuthority> authorities =
				authentication.getAuthorities() != null
						? authentication.getAuthorities()
						: AuthorityUtils.NO_AUTHORITIES;
		if (authorities.stream().anyMatch(a -> ADMIN_ROLE.equals(a.getAuthority()))) {
			log.warn("Admin user attempted user login: {}", authentication.getName());
			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			getRedirectStrategy().sendRedirect(request, response, "/admin/login?error=use_admin_login");
			return;
		}

		String returnUrl = request.getParameter(PARAM_RETURN_URL);
		if (returnUrl != null && !returnUrl.isBlank() && isSafeRedirect(returnUrl, request)) {
			getRedirectStrategy().sendRedirect(request, response, returnUrl);
			return;
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}

	/**
	 * 相対パス（/ 始まり）または同一ホストの絶対URLのみ許可（オープンリダイレクト対策）。
	 */
	private boolean isSafeRedirect(String url, HttpServletRequest request) {
		String u = url.trim();
		if (u.isEmpty()) return false;
		if (u.startsWith("/") && !u.startsWith("//")) {
			return true;
		}
		try {
			URI uri = new URI(u);
			if (uri.getHost() == null) return true;
			String requestHost = request.getServerName();
			return requestHost != null && requestHost.equalsIgnoreCase(uri.getHost());
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
