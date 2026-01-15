package com.example.flea_market_app.common.validation;

import java.util.regex.Pattern;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

public class EmailValidator {

	// emailの正規表現
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	public static void validate(String email) {
		if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_EMAIL, "error.invalid_email"

			);
		}
	}

}
