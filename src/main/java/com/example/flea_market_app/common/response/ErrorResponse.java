package com.example.flea_market_app.common.response;

public class ErrorResponse {

	private final String code;
	private final String message;

	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public static ErrorResponse of(String code, String message) {
		return new ErrorResponse(code, message);
	}

	public String getMessage() {
		return message;
	}

}
