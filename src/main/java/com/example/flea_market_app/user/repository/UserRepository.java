package com.example.flea_market_app.user.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.flea_market_app.user.domain.User;

public interface UserRepository {

	Optional<User> findById(UUID userId);

	default User getRequired(UUID userId) {

		return findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

	}

	void save(User user);

}
