package com.example.flea_market_app.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.user.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

	@Modifying
	@Query("""
			    UPDATE UserEntity u
			       SET u.active = :active
			     WHERE u.id = :userId
			""")
	int updateActive(
			@Param("userId") UUID userId,
			@Param("active") boolean active);
}
