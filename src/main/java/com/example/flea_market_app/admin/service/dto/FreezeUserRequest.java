package com.example.flea_market_app.admin.service.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreezeUserRequest {
	@NotBlank
	private String reason;
}
