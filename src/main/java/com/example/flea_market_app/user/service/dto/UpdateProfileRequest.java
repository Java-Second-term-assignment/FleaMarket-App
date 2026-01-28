package com.example.flea_market_app.user.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

	@NotBlank
	@Size(max = 50)
	private String displayName;
}
