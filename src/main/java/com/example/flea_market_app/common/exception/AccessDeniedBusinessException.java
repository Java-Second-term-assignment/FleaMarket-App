package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

public class AccessDeniedBusinessException extends BusinessException {

	public AccessDeniedBusinessException() {

		super(
				ErrorCode.PERMISSION_DENIED,
				"error.access_denied"

		);
	}

}
