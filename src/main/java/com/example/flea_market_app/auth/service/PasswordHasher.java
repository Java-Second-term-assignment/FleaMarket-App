package com.example.flea_market_app.auth.service;

public interface PasswordHasher {

	boolean matches(String rawPassword, String passwordHash);
}
