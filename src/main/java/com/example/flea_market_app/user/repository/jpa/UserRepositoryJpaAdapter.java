package com.example.flea_market_app.user.repository.jpa;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.flea_market_app.user.domain.User;
import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.domain.VerificationStatus;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryJpaAdapter implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final UserRankJpaRepository userRankJpaRepository;

	@Override
	public Optional<User> findById(UUID userId) {
		return userJpaRepository.findById(userId).map(this::toDomain);
	}

	@Override
	public void save(User user) {
		UserJpaEntity e = new UserJpaEntity();
		e.setId(user.getId());
		e.setDisplayName(user.getDisplayName());
		e.setUserRankId(user.getRank().getId());
		e.setIdentityStatus(user.getVerificationStatus().name());
		e.setActive(user.isActive());
		userJpaRepository.save(e);
	}

	private User toDomain(UserJpaEntity e) {
		UserRankJpaEntity rank = userRankJpaRepository.findById(e.getUserRankId())
				.orElseThrow(() -> new IllegalStateException("Rank not found: " + e.getUserRankId()));

		UserRank userRank = new UserRank(
				rank.getId(),
				rank.getCode(),
				rank.getName(),
				rank.getCommissionBps(),
				OffsetDateTime.now() // evaluatedAt（DB保持したければ列を追加）
		);

		return new User(
				e.getId(),
				e.getDisplayName(),
				VerificationStatus.valueOf(e.getIdentityStatus()),
				userRank,
				e.isActive());
	}
}
