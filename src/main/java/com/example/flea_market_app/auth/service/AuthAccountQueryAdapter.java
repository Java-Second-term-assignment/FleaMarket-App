package com.example.flea_market_app.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.user.service.port.AuthAccountQueryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthAccountQueryAdapter implements AuthAccountQueryPort {

	private final AuthUserRepository authUserRepository;

	@Override
	public Optional<String> findEmailByUserId(UUID userId) {
		return authUserRepository.findByUserId(userId)
				.map(e -> e.getEmail());
	}
}
