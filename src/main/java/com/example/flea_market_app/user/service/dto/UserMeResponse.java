package com.example.flea_market_app.user.service.dto;

import java.util.UUID;

import com.example.flea_market_app.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMeResponse {

	private UUID userId;
	private String displayName;
	private String email; // auth由来。userは保持しない
	private String verificationStatus;

	private String rankCode;
	private String rankName;
	private int commissionBps;

	private boolean active;

	public static UserMeResponse of(User user, String email) {
		return new UserMeResponse(
				user.getId(),
				user.getDisplayName(),
				email,
				user.getVerificationStatus().name(),
				user.getRank().getRankCode(),
				user.getRank().getRankName(),
				user.getRank().getCommissionBps(),
				user.isActive());
	}
}
