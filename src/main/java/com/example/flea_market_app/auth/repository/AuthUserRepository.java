package com.example.flea_market_app.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.auth.domain.AuthUserEntity;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID> {

	Optional<AuthUserEntity> findByUserId(UUID userId);

	Optional<AuthUserEntity> findByEmail(String email);
}
