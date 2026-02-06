package com.example.flea_market_app.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTest {

	private static final UUID USER_ID = UUID.randomUUID();

	@Mock
	private UserRank rank;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User(USER_ID, "Display", VerificationStatus.UNVERIFIED, rank, true);
	}

	@Nested
	class Constructor {
		@Test
		void validDisplayName_succeeds() {
			User u = new User(USER_ID, "Valid Name", VerificationStatus.UNVERIFIED, rank, true);
			assertThat(u.getDisplayName()).isEqualTo("Valid Name");
		}

		@Test
		void displayNameNull_throwsIllegalArgumentException() {
			assertThatThrownBy(() -> new User(USER_ID, null, VerificationStatus.UNVERIFIED, rank, true))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("displayName");
		}

		@Test
		void displayNameEmpty_throwsIllegalArgumentException() {
			assertThatThrownBy(() -> new User(USER_ID, "", VerificationStatus.UNVERIFIED, rank, true))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void displayNameOver50Chars_throwsIllegalArgumentException() {
			String longName = "a".repeat(51);
			assertThatThrownBy(() -> new User(USER_ID, longName, VerificationStatus.UNVERIFIED, rank, true))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("50");
		}
	}

	@Nested
	class SubmitVerification {
		@Test
		void whenUnverified_transitionsToPending() {
			user.submitVerification();
			assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
		}

		@Test
		void whenPending_idempotentDoesNotThrow() {
			user.submitVerification();
			user.submitVerification();
			assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
		}

		@Test
		void whenAlreadyVerified_throwsIllegalStateException() {
			User verifiedUser = new User(USER_ID, "Name", VerificationStatus.VERIFIED, rank, true);
			assertThatThrownBy(verifiedUser::submitVerification)
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("Already verified");
		}
	}
}
