package com.mysite.sns1_server.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.domain.member.service.MemberService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "회원 관련 API")
public class MemberController {
	private final MemberService memberService;

	@PostMapping(value = "/join", consumes = "application/json")
	@Operation(summary = "회원가입", description = "회원가입을 완료합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public CustomResponseBody<Void> join(@Valid @RequestBody JoinRequest request) {
		memberService.join(request);
		return CustomResponseBody.of("회원가입이 성공적으로 완료되었습니다.");
	}
}
