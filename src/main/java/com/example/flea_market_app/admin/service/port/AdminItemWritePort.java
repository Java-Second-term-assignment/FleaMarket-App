package com.example.flea_market_app.admin.service.port;

import java.util.UUID;

public interface AdminItemWritePort {
	/** true: 更新成功 / false: 対象なし */
	boolean markDeleted(UUID itemId);
}
