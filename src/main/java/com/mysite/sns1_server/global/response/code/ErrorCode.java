package com.mysite.sns1_server.global.response.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류"),

	// EMAIL DOMAIN
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,  "이메일 전송 실패"),
	EMAIL_VERIFY_FAILED(HttpStatus.BAD_REQUEST,  "이메일 인증 실패");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
