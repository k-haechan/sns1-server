package com.mysite.sns1_server.global.exception.handler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CustomResponseBody<List<String>>> handleValidationException(MethodArgumentNotValidException e) {
		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED; // 예시로 내부 서버 오류로 처리
		List<String> messages = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.sorted(
				Comparator.comparing(FieldError::getField)
					.thenComparing(error -> Optional.ofNullable(error.getDefaultMessage()).orElse(""))
			)
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.toList();

		return ResponseEntity.status(errorCode.getStatus())
			.body(CustomResponseBody.of(errorCode.getMessage(), messages));
	}

	/**
	 * 모든 예외 처리 (예상치 못한 서버 오류)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<CustomResponseBody<Void>> handleException(Exception e) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getStatus())
			.body(CustomResponseBody.of(errorCode.getMessage()));
	}
}
