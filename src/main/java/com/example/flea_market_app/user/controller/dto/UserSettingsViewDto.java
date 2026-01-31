package com.example.flea_market_app.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsViewDto {

	private String iconUrl;
	private String displayTitle;
	private String username;
	private String caption;
}
