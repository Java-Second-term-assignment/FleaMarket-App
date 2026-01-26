package com.example.flea_market_app.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {

	private final String userId;
	private final String passwordHash;
	private final String identifier;

	public static AuthUser of(String userId, String passwordHash, String identifier) {
		return new AuthUser(userId, passwordHash, identifier);
	}

}
