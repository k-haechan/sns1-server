package com.mysite.sns1_server.global.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.CustomResponseBody;
import com.mysite.sns1_server.global.response.code.ErrorCode;

@RestControllerAdvice
public class CustomExceptionHandler {
	/**
	 * 커스텀 예외 처리 (도메인/비즈니스 로직 기반 예외)
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<CustomResponseBody<Void>> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus())
			.body(CustomResponseBody.of(errorCode.getMessage()));
	}
}
