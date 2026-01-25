package com.example.flea_market_app.auth.service;

// 人が手動で設定したpasswordとの整合性チェック
public interface PasswordHasher {

	boolean matches(String rawPassword, String passwordHash);
}
