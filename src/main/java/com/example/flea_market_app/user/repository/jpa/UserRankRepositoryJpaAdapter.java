package com.example.flea_market_app.user.repository.jpa;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Repository;

import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.repository.UserRankRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRankRepositoryJpaAdapter implements UserRankRepository {

	private final UserRankJpaRepository userRankJpaRepository;

	@Override
	public UserRank getById(short id) {
		UserRankJpaEntity e = userRankJpaRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Rank not found: " + id));

		return new UserRank(
				e.getId(),
				e.getCode(),
				e.getName(),
				e.getCommissionBps(),
				OffsetDateTime.now());
	}

}
