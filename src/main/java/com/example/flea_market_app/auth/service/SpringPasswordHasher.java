package com.example.flea_market_app.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringPasswordHasher implements PasswordHasher {

	private final PasswordEncoder passwordEncoder;

	@Override
	public boolean matches(String rawPassword, String passwordhash) {

		return passwordEncoder.matches(rawPassword, passwordhash);
	}

}
