package com.example.flea_market_app.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {

	private final String userId;
	private final String passwordHash;

	public static AuthUser of(String userId, String passwordHash) {
		return new AuthUser(userId, passwordHash);
	}

}
