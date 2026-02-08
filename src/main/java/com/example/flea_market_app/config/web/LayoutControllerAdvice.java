package com.example.flea_market_app.config.web;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * レイアウト（ヘッダー等）で使う共通のモデル属性を追加する。
 * Thymeleaf 3.1 では #request がデフォルトで使えないため、
 * 管理者画面かどうかをモデル変数で渡す。
 */
@ControllerAdvice
public class LayoutControllerAdvice {

	/**
	 * 管理者コンテキスト＝管理者用の認証済み画面（ダッシュボード・ユーザー管理など）かどうか。
	 * /admin/login は未ログインでもアクセスする公開ページのため、false にして
	 * ヘッダーに「商品一覧」「掲示板一覧」を表示する。
	 */
	@ModelAttribute("isAdminContext")
	public boolean isAdminContext(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null || !uri.startsWith("/admin")) {
			return false;
		}
		// 管理者ログイン画面は一般向けの導線を表示する
		return !uri.equals("/admin/login") && !uri.startsWith("/admin/login?");
	}

	/**
	 * 現在のユーザーが管理者（ROLE_ADMIN）かどうか。
	 * 管理者ログイン後も一般ユーザー用のリンクをヘッダーに表示するために使用する。
	 */
	@ModelAttribute("isAdminUser")
	public boolean isAdminUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return false;
		}
		return auth.getAuthorities().stream()
				.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
	}
}
