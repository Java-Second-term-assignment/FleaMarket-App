package com.example.flea_market_app.user.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;

@Getter
public class UpdateProfileRequest {

	@NotBlank
	@Size(max = 50)
	private String displayName;

	public UpdateProfileCommand toCommand() {
		// trim等の前処理をcontroller層でやる方針ならここでOK
		return new UpdateProfileCommand(displayName == null ? null : displayName.trim());
	}
}