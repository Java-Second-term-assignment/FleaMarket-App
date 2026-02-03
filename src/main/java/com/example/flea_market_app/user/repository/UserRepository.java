package com.example.flea_market_app.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.user.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

	List<UserEntity> findByActiveFalse();

	@Modifying
	@Query("""
			    UPDATE UserEntity u
			       SET u.active = CASE WHEN u.active = true THEN false ELSE true END
			     WHERE u.id = :userId
			""")
	int toggleActive(@Param("userId") UUID userId);

	@Modifying
	@Query("""
			    UPDATE UserEntity u
			       SET u.displayName = :displayName
			     WHERE u.id = :userId
			""")
	int updateDisplayName(@Param("userId") UUID userId, @Param("displayName") String displayName);

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
