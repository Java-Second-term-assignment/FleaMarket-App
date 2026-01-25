package com.example.flea_market_app.auth.service;

public interface TokenHasher {

	String hash(String rawToken);
}
