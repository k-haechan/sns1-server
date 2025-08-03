package com.mysite.sns1_server.global.response.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	// General Server Error
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

	// Validation Errors
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "유효성 검사에 실패했습니다. 입력값을 확인해주세요."),

	// Email Domain Errors
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다. 이메일 주소를 확인하거나 잠시 후 다시 시도해주세요."),
	EMAIL_VERIFY_FAILED(HttpStatus.BAD_REQUEST, "이메일 인증에 실패했습니다. 인증 코드를 다시 확인해주세요."),
	EMAIL_VERIFY_EXPIRED(HttpStatus.BAD_REQUEST, "이메일 인증 코드가 만료되었습니다. 새로운 인증 코드를 요청해주세요."),

	// DB Errors
	USERNAME_DUPLICATE(HttpStatus.CONFLICT, "이미 사용 중인 사용자 이름입니다."),
	EMAIL_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 이메일 주소입니다."),
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

	// Authentication Errors
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,"토큰이 만료되었습니다. 다시 로그인해주세요."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다. 다시 로그인해주세요."),

	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다. 다시 시도해주세요."),
	BAD_CREDENTIAL(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다. 다시 시도해주세요."),

	// Chat Room Errors
	CHAT_ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방에 접속할 수 없습니다."),

	// Post Errors
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "리소스에 접근할 수 없습니다."),

	// Follow Errors
	FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 팔로우 요청입니다." ),
	INVALID_FOLLOW_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 팔로우 요청입니다." ),
	ALREADY_REQUESTED(HttpStatus.BAD_REQUEST, "이미 팔로우 요청을 보냈습니다." ),
	ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "이미 팔로우 관계 입니다." ),

	// Comment Errors
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다." ),

	// Notification Errors
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다." ),;


	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
