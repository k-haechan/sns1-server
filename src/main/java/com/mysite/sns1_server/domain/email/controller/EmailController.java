package com.mysite.sns1_server.domain.email.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.email.dto.request.CodeRequest;
import com.mysite.sns1_server.domain.email.dto.request.VerifyRequest;
import com.mysite.sns1_server.domain.email.service.EmailService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Tag(name = "Email", description = "이메일 관련 API")
public class EmailController {
	private final EmailService emailService;

	@PostMapping("/code")
	@Operation(summary = "이메일 인증코드 전송", description = "이메일로 인증코드를 전송합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Void> sendCode(@Valid @RequestBody CodeRequest emailRequest) {
		String email = emailRequest.email();
		emailService.sendCode(email);

		return CustomResponseBody.of("이메일 인증코드가 성공적으로 전송되었습니다.");
	}

	@PostMapping("/code/verify")
	@Operation(summary = "인증코드 유효성 확인", description = "인증코드의 유효성을 확인합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Void> verifyCode(@Valid @RequestBody VerifyRequest verifyRequest) {
		String email = verifyRequest.email();
		String code = verifyRequest.code();

		emailService.verifyCode(email, code);
		return CustomResponseBody.of("인증코드가 성공적으로 확인되었습니다. 10분 동안 유효합니다.");
	}
}
