package com.example.flea_market_app.admin.service.port;

import java.util.UUID;

public interface AdminUserWritePort {
	boolean setActive(UUID userId, boolean active);

	boolean setAdminByUserId(UUID userId, boolean admin);
}
