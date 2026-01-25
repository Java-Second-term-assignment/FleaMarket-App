package com.example.flea_market_app.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.auth.domain.RefreshToken;

@Service
public class TokenService {

	public String generateAccessToken(String userId) {

		// JWT生成(簡略)
		return "access-" + UUID.randomUUID();
	}

	public RefreshToken generateRefreshToken(String userId) {

		return new RefreshToken(
				UUID.randomUUID().toString(),
				userId,
				LocalDateTime.now().plusDays(30));
	}
}
