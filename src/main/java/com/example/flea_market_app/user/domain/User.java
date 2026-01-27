package com.example.flea_market_app.user.domain;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

@Getter
public class User {

	private final UUID id;
	private String displayName;
	private VerificationStatus verificationStatus;
	private UserRank rank;
	private boolean active;

	public User(
			UUID id,
			String displayName,
			VerificationStatus verificationStatus,
			UserRank rank,
			boolean active) {

		this.id = Objects.requireNonNull(id);
		this.displayName = requireText(displayName, "displayName");
		this.verificationStatus = Objects.requireNonNull(verificationStatus);
		this.rank = Objects.requireNonNull(rank);
		this.active = active;

	}

	public void updateProfile(String newDisplayName) {

		this.displayName = requireText(newDisplayName, "displayName");
	}

	public void submitVerification() {
		// 不正遷移を防ぐ
		if (this.verificationStatus == VerificationStatus.VERIFIED) {
			throw new IllegalStateException("Already verified");

		}
		if (this.verificationStatus == VerificationStatus.PENDING) {
			return;

		}
		this.verificationStatus = VerificationStatus.PENDING;

	}

	public void approveVerification() {

		if (this.verificationStatus != VerificationStatus.PENDING) {
			throw new IllegalStateException("Invalid transition to VERIFIED");
		}

		this.verificationStatus = VerificationStatus.VERIFIED;
	}

	public void rejectVerification() {

		if (this.verificationStatus != VerificationStatus.PENDING) {
			throw new IllegalStateException("Invalid transition to REJECTED");
		}

		this.verificationStatus = VerificationStatus.REJECTED;
	}

	public void changeRank(UserRank newRank) {
		this.rank = Objects.requireNonNull(newRank);
	}

	private static String requireText(String v, String field) {

		if (v == null)
			throw new IllegalArgumentException(field + " is required");
		String t = v.trim();
		if (t.isEmpty())
			throw new IllegalArgumentException(field + " must be <= 50 chars");
		if (t.length() > 50)
			throw new IllegalArgumentException(field + " must be <= 50 chars");

		return t;
	}

}
