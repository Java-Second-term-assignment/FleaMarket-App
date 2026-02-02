package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

// 直接インスタンス化されないようにするための抽象クラス

public abstract class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;
	private final String messageKey;

	protected BusinessException(ErrorCode errorCode, String messageKey) {
		super(messageKey);

		this.errorCode = errorCode;
		this.messageKey = messageKey;
	}

	protected BusinessException(
			ErrorCode errorCode,
			String messageKey,
			Throwable cause) {
		super(messageKey, cause);
		this.errorCode = errorCode;
		this.messageKey = messageKey;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getMessageKey() {
		return messageKey;
	}
}
