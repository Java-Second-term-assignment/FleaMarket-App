package com.example.flea_market_app.common.validation;

import java.util.regex.Pattern;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

public class PasswordPolicy {

	// バリデーションチェック(10文字以上64文字以内、英数字・記号全て必須)
	private static final Pattern PASSWORD_PATTERN = Pattern
			.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\\d!@#$%^&*()_+=-]{10,64}$");

	public static void validate(String password) {

		if (password == null) {

			throw new ValidationBusinessException(
					ErrorCode.INVALID_PASSWORD,
					"error.password.required");
		}

		if (!PASSWORD_PATTERN.matcher(password).matches()) {

			throw new ValidationBusinessException(
					ErrorCode.INVALID_PASSWORD,
					"error.password.policy");
		}

	}
}
