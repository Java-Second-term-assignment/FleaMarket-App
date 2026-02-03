package com.example.flea_market_app.config.web;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * レイアウト（ヘッダー等）で使う共通のモデル属性を追加する。
 * Thymeleaf 3.1 では #request がデフォルトで使えないため、
 * 管理者画面かどうかをモデル変数で渡す。
 */
@ControllerAdvice
public class LayoutControllerAdvice {

	@ModelAttribute("isAdminContext")
	public boolean isAdminContext(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri != null && uri.startsWith("/admin");
	}
}
