package com.example.flea_market_app.user.service.port;

import java.util.Optional;
import java.util.UUID;

public interface AuthAccountQueryPort {

	Optional<String> findEmailByUserId(UUID userId);

}
