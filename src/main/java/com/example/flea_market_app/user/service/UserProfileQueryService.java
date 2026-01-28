package com.example.flea_market_app.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.user.service.dto.UserMeResponse;
import com.example.flea_market_app.user.service.port.AuthAccountQueryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileQueryService {

	private final UserService userService;
	private final AuthAccountQueryPort authAccountQueryPort;

	@Transactional(readOnly = true)
	public UserMeResponse getMe(UUID userId) {
		var user = userService.getRequired(userId);

		String email = authAccountQueryPort.findEmailByUserId(userId).orElse(null);

		return UserMeResponse.of(user, email);
	}
}
