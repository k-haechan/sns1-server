package com.mysite.sns1_server.domain.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.common.util.CookieUtil;
import com.mysite.sns1_server.domain.auth.dto.CodeRequest;
import com.mysite.sns1_server.domain.auth.dto.LoginRequest;
import com.mysite.sns1_server.domain.auth.dto.VerifyRequest;
import com.mysite.sns1_server.domain.auth.service.EmailService;
import com.mysite.sns1_server.domain.member.service.MemberService;
import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.response.CustomResponseBody;
import com.mysite.sns1_server.global.security.jwt.service.AccessTokenService;
import com.mysite.sns1_server.global.security.jwt.service.RefreshTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
	private final EmailService emailService;
	private final MemberService memberService;
	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;
	private final RedisService redisService;

	@PostMapping( value = "/login", consumes = "application/json")
	@Operation(summary = "로그인", description = "회원 정보를 기반으로 로그인합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Void> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		Long memberId = memberService.login(request);

		// 액세스 토큰 생성 및 쿠키 설정
		CookieUtil.setCookie(
			response,
			accessTokenService.getTokenName(),
			accessTokenService.generateToken(memberId),
			accessTokenService.getExpiration()
		);

		// 리프레시 토큰 생성 및 쿠키 설정
		CookieUtil.setCookie(
			response,
			refreshTokenService.getTokenName(),
			refreshTokenService.generateToken(memberId),
			refreshTokenService.getExpiration()
		);

		return CustomResponseBody.of("로그인 성공, 토큰이 생성되었습니다.");
	}

	@PostMapping(value = "logout")
	@Operation(summary = "로그아웃", description = "로그아웃을 수행합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		String accessTokenName = accessTokenService.getTokenName();
		String refreshTokenName = refreshTokenService.getTokenName();

		String refreshToken = CookieUtil.extractCookie(request, refreshTokenName);

		// 블랙리스트에 토큰 추가 (로그아웃 시 토큰 무효화)
		redisService.set(
			RedisKeyType.BLACKLIST,
			refreshToken,
			"logout",
			refreshTokenService.getLeftExpirationTime(refreshToken)
		);

		// 쿠키에서 액세스 토큰과 리프레시 토큰 제거
		CookieUtil.deleteCookie(response, accessTokenName);
		CookieUtil.deleteCookie(response, refreshTokenName);

		// SecurityContext에서 인증 정보 제거
		SecurityContextHolder.clearContext();

		return CustomResponseBody.of("로그아웃이 성공적으로 완료되었습니다.");
	}

	@PostMapping(value = "email/code", consumes = "application/json")
	@Operation(summary = "이메일 인증코드 전송", description = "이메일로 인증코드를 전송합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Void> sendCode(@Valid @RequestBody CodeRequest codeRequest) {
		String email = codeRequest.email();
		emailService.sendCode(email);

		return CustomResponseBody.of("이메일 인증코드가 성공적으로 전송되었습니다.");
	}

	@PostMapping(value = "email/code/verify", consumes = "application/json")
	@Operation(summary = "인증코드 유효성 확인", description = "인증코드의 유효성을 확인합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Void> verifyCode(@Valid @RequestBody VerifyRequest verifyRequest) {
		String email = verifyRequest.email();
		String code = verifyRequest.code();

		emailService.verifyCode(email, code);
		return CustomResponseBody.of("인증코드가 성공적으로 확인되었습니다. 10분 동안 유효합니다.");
	}
}
