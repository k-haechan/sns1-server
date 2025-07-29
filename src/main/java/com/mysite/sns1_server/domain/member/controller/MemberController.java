package com.mysite.sns1_server.domain.member.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.member.dto.request.JoinRequest;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
import com.mysite.sns1_server.domain.member.dto.response.MemberDetailResponse;
import com.mysite.sns1_server.domain.member.service.MemberService;
import com.mysite.sns1_server.domain.post.dto.PostResponse;
import com.mysite.sns1_server.domain.post.service.PostService;
import com.mysite.sns1_server.global.aws.cloudfront.service.CloudFrontService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "회원 관련 API")
public class MemberController {
	private final MemberService memberService;
	private final PostService postService;
	private final CloudFrontService cloudFrontService;

	@PostMapping(value = "/join", consumes = "application/json")
	@Operation(summary = "회원가입", description = "회원가입을 완료합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public CustomResponseBody<Void> join(@Valid @RequestBody JoinRequest request) {
		memberService.join(request);
		return CustomResponseBody.of("회원가입이 성공적으로 완료되었습니다.");
	}


	@GetMapping("/{member_id}")
	@Operation(summary = "회원 정보 조회", description = "특정 회원의 정보를 조회합니다.")
	public CustomResponseBody<MemberDetailResponse> getMemberInfo(@PathVariable("member_id") Long memberId) {
		MemberDetailResponse result = memberService.getMemberInfo(memberId);
		return CustomResponseBody.of("회원 정보 조회가 성공적으로 완료되었습니다.", result);
	}

	@GetMapping
	@Operation(summary = "회원 이름 검색", description = "회원 이름으로 회원을 검색합니다.")
	public CustomResponseBody<MemberBriefResponse> searchMemberByName(@RequestParam String username) {
		MemberBriefResponse result = memberService.searchMemberByUsername(username);
		return CustomResponseBody.of("회원 이름 검색이 성공적으로 완료되었습니다.", result);
	}

	@GetMapping("/{member_id}/posts")
	@Operation(summary = "회원 게시글 조회", description = "특정 회원의 게시글을 조회합니다.")
	public CustomResponseBody<Slice<PostResponse>> getMemberPosts(@PathVariable("member_id") Long memberId, @RequestParam(value = "cursor-id", required = false) Long cursorId,
		Pageable pageable, HttpServletResponse response) {
		Slice<PostResponse> result = postService.findPosts(memberId, cursorId, pageable);

		cloudFrontService.generateSignedCookies(memberId, response);
		return CustomResponseBody.of("회원의 게시물 조회가 성공적으로 완료되었습니다.", result);
	}
}
