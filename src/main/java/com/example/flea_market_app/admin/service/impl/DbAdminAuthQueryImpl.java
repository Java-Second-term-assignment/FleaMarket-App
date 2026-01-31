package com.example.flea_market_app.admin.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.flea_market_app.admin.service.port.AdminAuthQueryPort;
import com.example.flea_market_app.auth.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DbAdminAuthQueryImpl implements AdminAuthQueryPort {

	private final AuthUserRepository authUserRepository;

	@Override
	public Optional<AdminAuthInfo> findByUserId(UUID userId) {
		return authUserRepository.findByUserId(userId)
				.map(u -> new AdminAuthInfo(u.getId(), u.isAdmin()));
	}
}
