package com.example.flea_market_app.auth.service.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class RefreshRequest {

	@NotBlank
	private String refreshToken;
}
