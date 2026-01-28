package com.example.flea_market_app.user.service;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.user.domain.UserRank;
import com.example.flea_market_app.user.repository.UserRankRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRankService {

	private final UserRankRepository userRankRepository;

	public UserRank loadRank(short rankId) {
		return userRankRepository.findById(rankId)
				.orElseThrow(() -> new IllegalArgumentException("Rank not found: " + rankId));
	}
}
