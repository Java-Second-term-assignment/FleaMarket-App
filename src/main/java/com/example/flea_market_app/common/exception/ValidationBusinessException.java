package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

public class ValidationBusinessException extends BusinessException {

	public ValidationBusinessException(ErrorCode errorCode, String messageKey) {

		super(errorCode, messageKey);
	}

}
