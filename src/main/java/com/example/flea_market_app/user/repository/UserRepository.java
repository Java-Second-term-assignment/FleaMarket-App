package com.example.flea_market_app.user.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.user.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

	/**
	 * 配送先表示用に recipient_name, postal_code, address のみ取得する。
	 * notification_enabled 等を参照しないため、カラム未追加のDBでも動作する。
	 */
	@Query("SELECT u.recipientName AS recipientName, u.postalCode AS postalCode, u.address AS address FROM UserEntity u WHERE u.id = :userId")
	Optional<ShippingAddressProjection> findShippingAddressByUserId(@Param("userId") UUID userId);

	interface ShippingAddressProjection {
		String getRecipientName();
		String getPostalCode();
		String getAddress();
	}

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

	@Modifying
	@Query("""
			    UPDATE UserEntity u
			       SET u.active = :active, u.frozenUntil = :frozenUntil
			     WHERE u.id = :userId
			""")
	int updateActiveAndFrozenUntil(
			@Param("userId") UUID userId,
			@Param("active") boolean active,
			@Param("frozenUntil") OffsetDateTime frozenUntil);

	@Modifying
	@Query("""
			    UPDATE UserEntity u
			       SET u.active = true, u.frozenUntil = null
			     WHERE u.id = :userId
			       AND u.active = false
			       AND u.frozenUntil IS NOT NULL
			       AND u.frozenUntil <= :now
			""")
	int restoreExpiredFreeze(
			@Param("userId") UUID userId,
			@Param("now") OffsetDateTime now);

	@Modifying
	@Query("""
			    UPDATE UserEntity u
			       SET u.notificationEnabled = :enabled
			     WHERE u.id = :userId
			""")
	int updateNotificationEnabled(@Param("userId") UUID userId, @Param("enabled") boolean enabled);
}
