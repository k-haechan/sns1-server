package com.mysite.sns1_server.global.response;

public record CustomResponseBody<T>(
	String message,
	T data
) {
	public static <T> CustomResponseBody<T> of(String message, T data) {
		return new CustomResponseBody<>(message, data);
	}

	public static CustomResponseBody<Void> of(String message) {
		return new CustomResponseBody<>(message, null);
	}
}
