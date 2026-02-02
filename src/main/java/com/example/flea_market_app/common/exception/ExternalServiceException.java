package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

public class ExternalServiceException extends BusinessException {

	public ExternalServiceException(ErrorCode errorCode, String messageKey, Throwable cause) {
		super(errorCode, messageKey, cause);
	}

	public ExternalServiceException(ErrorCode errorCode, String messageKey) {
		super(errorCode, messageKey);
	}
}
