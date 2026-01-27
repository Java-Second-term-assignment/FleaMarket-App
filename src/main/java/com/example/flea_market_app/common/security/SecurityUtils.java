package com.example.flea_market_app.common.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.BusinessException;

/**
 * セキュリティ関連のユーティリティクラス。
 * 
 * <p>認証済みユーザーの情報を取得するためのユーティリティメソッドを提供します。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public class SecurityUtils {

	/**
	 * 認証済みユーザーのIDを取得します。
	 * 
	 * <p>SecurityContextから認証情報を取得し、ユーザーIDを返却します。
	 * 認証されていない場合はBusinessExceptionをスローします。
	 * 
	 * <p>注意: 現在の実装では、AuthenticationのprincipalがユーザーID（UUIDの文字列）であることを想定しています。
	 * 実際の認証実装に応じて、このメソッドを調整する必要があります。
	 * 
	 * @return 認証済みユーザーのID
	 * @throws BusinessException 認証されていない場合
	 */
	public static UUID getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "error.unauthorized") {
			};
		}

		try {
			String principal = authentication.getPrincipal().toString();
			return UUID.fromString(principal);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "error.unauthorized") {
			};
		}
	}

}
