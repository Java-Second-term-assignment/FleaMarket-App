package com.example.flea_market_app.auth.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.auth.service.AuthService;
import com.example.flea_market_app.auth.service.dto.LoginRequest;
import com.example.flea_market_app.auth.service.dto.LoginResponse;
import com.example.flea_market_app.auth.service.dto.RefreshRequest;
import com.example.flea_market_app.auth.service.dto.RefreshResponse;

import lombok.RequiredArgsConstructor;

/**
 * 認証 API。レスポンスは ResponseEntity / ApiResponse でラップしていない。
 * タイムリーフで統一形式を期待する場合は要対応。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/** 一般ユーザー用ログイン（API） */
	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	/** 管理者用ログイン（API）。非管理者は 403。一般ユーザーとはエントリーポイントを分離。 */
	@PostMapping("/admin/login")
	public LoginResponse adminLogin(@Valid @RequestBody LoginRequest request) {
		return authService.adminLogin(request);
	}

	@PostMapping("/refresh")
	public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {

		return authService.refresh(request);

	}

}
