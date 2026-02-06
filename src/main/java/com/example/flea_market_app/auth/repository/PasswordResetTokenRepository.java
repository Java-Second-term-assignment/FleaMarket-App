package com.example.flea_market_app.auth.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.auth.domain.PasswordResetTokenEntity;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

	Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

	@Modifying
	@Query("DELETE FROM PasswordResetTokenEntity t WHERE t.userId = :userId")
	int deleteByUserId(@Param("userId") UUID userId);
}
