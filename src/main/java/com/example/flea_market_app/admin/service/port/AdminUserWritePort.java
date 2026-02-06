package com.example.flea_market_app.admin.service.port;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AdminUserWritePort {
	boolean setActive(UUID userId, boolean active);

	boolean setActiveAndFrozenUntil(UUID userId, boolean active, OffsetDateTime frozenUntil);

	boolean toggleActive(UUID userId);

	boolean setAdminByUserId(UUID userId, boolean admin);

	boolean updateUserProfile(UUID userId, String displayName, String email);

	boolean deleteUserPermanently(UUID userId);
}
