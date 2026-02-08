package com.example.flea_market_app.common.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.flea_market_app.common.exception.ValidationBusinessException;

class PasswordPolicyTest {

	@Test
	void validate_null_throwsValidationBusinessException() {
		assertThatThrownBy(() -> PasswordPolicy.validate(null))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_tooShort_throwsValidationBusinessException() {
		assertThatThrownBy(() -> PasswordPolicy.validate("Ab1!"))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_noLetter_throwsValidationBusinessException() {
		assertThatThrownBy(() -> PasswordPolicy.validate("1234567890!"))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_noDigit_throwsValidationBusinessException() {
		assertThatThrownBy(() -> PasswordPolicy.validate("Abcdefghij!"))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_noSpecialChar_throwsValidationBusinessException() {
		assertThatThrownBy(() -> PasswordPolicy.validate("Abcdefgh12"))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_validPassword_doesNotThrow() {
		assertThatCode(() -> PasswordPolicy.validate("ValidPass1!"))
				.doesNotThrowAnyException();
	}
}
