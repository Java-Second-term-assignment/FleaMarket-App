package com.example.flea_market_app.auth.service;

import com.example.flea_market_app.auth.domain.AuthUser;

public interface AuthUserProvider {

	AuthUser loadByIdentifier(String identifier);
}
