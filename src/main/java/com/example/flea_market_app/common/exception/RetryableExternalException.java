package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

public class RetryableExternalException extends BusinessException {

	public RetryableExternalException(ErrorCode errorCode, String messageKey, Throwable cause) {
		super(errorCode, messageKey, cause);
	}
}
