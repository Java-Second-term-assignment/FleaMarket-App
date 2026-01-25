package com.example.flea_market_app.auth.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

	private String userId;
	private String accessToken;
	private String refreshToken;

}
