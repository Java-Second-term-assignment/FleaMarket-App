package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

public class NotFoundBusinessException extends BusinessException {

	public NotFoundBusinessException() {

		super(
				ErrorCode.USER_NOT_FOUND,
				"error.not_found"

		);
	}

}
