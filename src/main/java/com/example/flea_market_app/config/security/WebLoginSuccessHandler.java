package com.example.flea_market_app.config.security;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

/**
 * 一般ユーザー用ログイン成功ハンドラ。
 * リクエストに returnUrl パラメータがあればそこへリダイレクト（オープンリダイレクト対策済み）。
 * なければデフォルトの /products へ。
 */
public class WebLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

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
