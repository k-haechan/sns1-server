package com.mysite.sns1_server.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.domain.member.dto.MemberInfoResponse;
import com.mysite.sns1_server.domain.member.dto.MemberResponse;
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


	@GetMapping("/{memberId}")
	@Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
	public CustomResponseBody<MemberInfoResponse> getMemberInfo(@PathVariable Long memberId) {
		MemberInfoResponse response = memberService.getMemberInfo(memberId);
		return CustomResponseBody.of("회원 정보 조회가 성공적으로 완료되었습니다.", response);
	}

	@GetMapping
	@Operation(summary = "회원 이름 검색", description = "회원 이름으로 회원을 검색합니다.")
	public CustomResponseBody<MemberResponse> searchMemberByName(@RequestParam String username) {
		MemberResponse response = memberService.searchMemberByUsername(username);
		return CustomResponseBody.of("회원 이름 검색이 성공적으로 완료되었습니다.", response);
	}
}
