package com.example.flea_market_app.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.auth.domain.AuthUserEntity;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID> {

	Optional<AuthUserEntity> findByUserId(UUID userId);

	Optional<AuthUserEntity> findByEmail(String email);

	@Modifying
	@Query("""
			    UPDATE AuthUserEntity a
			       SET a.admin = :admin
			     WHERE a.userId = :userId
			""")
	int updateAdmin(
			@Param("userId") UUID userId,
			@Param("admin") boolean admin);
}
