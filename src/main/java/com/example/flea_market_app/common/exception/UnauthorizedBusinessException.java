package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

public class UnauthorizedBusinessException extends BusinessException {

	public UnauthorizedBusinessException() {
		super(
				ErrorCode.UNAUTHORIZED,
				"error.unauthorized");
	}

	// auth向けのファクトリ（意図が読みやすくなる）
	public static UnauthorizedBusinessException invalidCredentials() {
		return new UnauthorizedBusinessException();
	}

	public static UnauthorizedBusinessException invalidRefreshToken() {
		return new UnauthorizedBusinessException();
	}

	public static UnauthorizedBusinessException refreshTokenExpired() {
		return new UnauthorizedBusinessException();
	}
}
