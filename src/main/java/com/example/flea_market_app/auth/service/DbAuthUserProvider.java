package com.example.flea_market_app.auth.service;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.auth.domain.AuthUser;
import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DbAuthUserProvider implements AuthUserProvider {

	private final AuthUserRepository authUserRepository;

	@Override
	public AuthUser loadByIdentifier(String identifier) {
		AuthUserEntity entity = authUserRepository.findByEmail(identifier)
				.orElseThrow(() -> new IllegalArgumentException("not found"));

		return AuthUser.of(
				entity.getUserId().toString(),
				entity.getPasswordHash(),
				entity.getEmail());
	}

}
