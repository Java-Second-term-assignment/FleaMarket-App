package com.example.flea_market_app.admin.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.service.port.AdminUserWritePort;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DbAdminUserWriteImpl implements AdminUserWritePort {

	private final UserRepository userRepository;
	private final AuthUserRepository authUserRepository;

	@Override
	@Transactional
	public boolean setActive(UUID userId, boolean active) {
		return userRepository.updateActive(userId, active) > 0;
	}

	@Override
	@Transactional
	public boolean toggleActive(UUID userId) {
		return userRepository.toggleActive(userId) > 0;
	}

	@Override
	@Transactional
	public boolean setAdminByUserId(UUID userId, boolean admin) {
		return authUserRepository.updateAdmin(userId, admin) > 0;
	}

	@Override
	@Transactional
	public boolean updateUserProfile(UUID userId, String displayName, String email) {
		int u = userRepository.updateDisplayName(userId, displayName);
		int a = authUserRepository.updateEmailByUserId(userId, email);
		return u > 0 && a > 0;
	}

	@Override
	@Transactional
	public boolean deleteUserPermanently(UUID userId) {
		if (!userRepository.existsById(userId))
			return false;
		authUserRepository.findByUserId(userId).ifPresent(authUserRepository::delete);
		userRepository.deleteById(userId);
		return true;
	}
}
