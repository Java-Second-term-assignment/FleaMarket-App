package com.example.flea_market_app.admin.service.port;

import java.util.Optional;
import java.util.UUID;

public interface AdminAuthQueryPort {

	Optional<AdminAuthInfo> findByUserId(UUID userId);

	class AdminAuthInfo {
		private final UUID authUserId; // auth_users.id
		private final boolean admin;

		public AdminAuthInfo(UUID authUserId, boolean admin) {
			this.authUserId = authUserId;
			this.admin = admin;
		}

		public UUID getAuthUserId() {
			return authUserId;
		}

		public boolean isAdmin() {
			return admin;
		}
	}
}
