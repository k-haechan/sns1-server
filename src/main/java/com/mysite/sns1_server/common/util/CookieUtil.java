package com.mysite.sns1_server.common.util;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieUtil {

	private CookieUtil() {} // 유틸 클래스이므로 인스턴스 생성 방지

	public static void setCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)             // 자바스크립트 접근 방지
			.secure(true)               // HTTPS 환경에서만 전송
			.path("/")                  // 전체 경로에서 쿠키 접근 가능
			.maxAge(maxAge)             // Duration을 초로 자동 변환
			.sameSite("Lax")         // 크로스 사이트 요청 시 쿠키 미포함
			.build();

		response.addHeader("Set-Cookie", cookie.toString());
	}

	public static String extractCookie(HttpServletRequest request, String name) {
		if (request.getCookies() == null) return null;

		for (Cookie cookie : request.getCookies()) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public static void deleteCookie(HttpServletResponse response, String tokenName) {
		setCookie(response, tokenName, "", Duration.ofSeconds(0));
	}
}
