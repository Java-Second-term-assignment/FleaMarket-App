package com.example.flea_market_app.common.response;

public class ApiResponse<T> {

	private final boolean success;
	private final T data;

	public ApiResponse(boolean success, T data) {
		this.success = success;
		this.data = data;
	}

	// 成功レスポンス用
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data);
	}

	// データなし成功(DELETE など)
	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(true, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public T getData() {
		return data;
	}

}
