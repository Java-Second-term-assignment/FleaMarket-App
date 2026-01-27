package com.example.flea_market_app.user.controller;

import java.util.UUID;

import com.example.flea_market_app.user.domain.User;

import lombok.Value;

@Value
public class UserMeResponse {
	UUID userId;
	String displayName;
	String email;
	String verificationStatus;
	String rankCode;
	String rankName;
	int commissionBps;
	boolean active;

	public static UserMeResponse of(User user, String email) {
		return new UserMeResponse(
				user.getId(),
				user.getDisplayName(),
				email,
				user.getVerificationStatus().name(),
				user.getRank().getCode(),
				user.getRank().getName(),
				user.getRank().getCommissionBps(),
				user.isActive());
	}
}