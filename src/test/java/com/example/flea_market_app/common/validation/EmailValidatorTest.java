package com.example.flea_market_app.common.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.flea_market_app.common.exception.ValidationBusinessException;

class EmailValidatorTest {

	@Test
	void validate_null_throwsValidationBusinessException() {
		assertThatThrownBy(() -> EmailValidator.validate(null))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_invalidFormat_throwsValidationBusinessException() {
		assertThatThrownBy(() -> EmailValidator.validate("not-an-email"))
				.isInstanceOf(ValidationBusinessException.class);
		assertThatThrownBy(() -> EmailValidator.validate("@nodomain.com"))
				.isInstanceOf(ValidationBusinessException.class);
		assertThatThrownBy(() -> EmailValidator.validate("noatsign.com"))
				.isInstanceOf(ValidationBusinessException.class);
	}

	@Test
	void validate_validEmail_doesNotThrow() {
		assertThatCode(() -> EmailValidator.validate("user@example.com"))
				.doesNotThrowAnyException();
		assertThatCode(() -> EmailValidator.validate("test+tag@domain.co.jp"))
				.doesNotThrowAnyException();
	}
}
