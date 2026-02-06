package com.example.flea_market_app.auth.repository;

import java.time.OffsetDateTime;
import java.util.List;
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

	@Modifying
	@Query("""
			    UPDATE AuthUserEntity a
			       SET a.email = :email
			     WHERE a.userId = :userId
			""")
	int updateEmailByUserId(@Param("userId") UUID userId, @Param("email") String email);

	@Modifying
	@Query("""
			    UPDATE AuthUserEntity a
			       SET a.passwordHash = :passwordHash
			     WHERE a.userId = :userId
			""")
	int updatePasswordHashByUserId(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash);

	/** 指定日以降の日別登録数（グラフ用） */
	@Query(value = """
			SELECT (created_at AT TIME ZONE 'UTC')::date AS day, COUNT(*) FROM auth_users
			WHERE created_at >= :since
			GROUP BY (created_at AT TIME ZONE 'UTC')::date ORDER BY day
			""", nativeQuery = true)
	List<Object[]> countByDaySince(@Param("since") OffsetDateTime since);
}
