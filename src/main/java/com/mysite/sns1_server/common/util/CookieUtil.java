package com.mysite.sns1_server.common.util;

import static org.springframework.boot.web.server.Cookie.SameSite.*;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieUtil {

	private CookieUtil() {} // 유틸 클래스이므로 인스턴스 생성 방지

	public static void setCookie(HttpServletResponse response, String name, String value,
		String domain, String path, long maxAge) {

		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(true)
			.domain(domain)
			.path(path)
			.maxAge(maxAge)
			.sameSite(NONE.toString())
			.build();

		response.addHeader("Set-Cookie", cookie.toString());
	}

	public static void setCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)             // 자바스크립트 접근 방지 (권장)
			.secure(true)               // 로컬 개발(HTTP)을 위해 false. 배포 시에는 true로 변경!
			.path("/")                  // 전체 경로에서 쿠키 접근 가능
			.maxAge(maxAge)             // Duration을 초로 자동 변환
			// .domain("localhost")     // <--- 추가: localhost 도메인 전체에서 유효하게 함
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
