package com.example.flea_market_app.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.user.domain.User;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.VerificationStatus;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserRankService userRankService;

	@Transactional(readOnly = true)
	public User getRequired(UUID userId) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		return toDomain(e);
	}

	@Transactional
	public void updateProfile(UUID userId, String displayName) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		e.setDisplayName(displayName.trim());
		userRepository.save(e);
	}

	@Transactional
	public void submitVerification(UUID userId) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		// 状態遷移の妥当性は domain でチェックしたいので一度 domain 化してもOK
		User user = toDomain(e);
		user.submitVerification();

		e.setIdentityStatus(user.getVerificationStatus().name());
		userRepository.save(e);
	}

	private User toDomain(UserEntity e) {
		return new User(
				e.getId(),
				e.getDisplayName(),
				VerificationStatus.valueOf(e.getIdentityStatus()),
				userRankService.loadRank(e.getUserRankId()),
				e.isActive());
	}
}
