package com.example.flea_market_app.admin.service.port;

import java.util.UUID;

public interface AdminUserWritePort {
	boolean setActive(UUID userId, boolean active);

	boolean toggleActive(UUID userId);

	boolean setAdminByUserId(UUID userId, boolean admin);

	boolean updateUserProfile(UUID userId, String displayName, String email);

	boolean deleteUserPermanently(UUID userId);
}
