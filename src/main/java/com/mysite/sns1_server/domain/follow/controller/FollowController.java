package com.mysite.sns1_server.domain.follow.controller;

import java.security.Principal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.follow.dto.response.FollowResponse;
import com.mysite.sns1_server.domain.follow.service.FollowService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관련 API")
public class FollowController {
	private final FollowService followService;
	@PostMapping("/members/{member_id}")
	@Operation(summary = "팔로우 요청", description = "특정 회원을 팔로우 요청합니다.")
	public CustomResponseBody<FollowResponse> requestFollow(@PathVariable("member_id") Long followerId, Principal principal) {
		Long followingId = Long.parseLong(principal.getName());
		FollowResponse result = followService.requestFollow(followingId, followerId);

		return CustomResponseBody.of("팔로우 요청이 성공적으로 완료되었습니다.", result);
	}

	@GetMapping
	@Operation(summary = "팔로우 요청 조회", description = "내 팔로우 요청을 조회합니다.")
	public CustomResponseBody<Slice<FollowResponse>> findMyFollow(Principal principal, Pageable pageable) {
		Slice<FollowResponse> result = followService.findMyFollow(principal, pageable);
		return CustomResponseBody.of("팔로우 요청 조회가 성공적으로 완료되었습니다.", result);
	}

	@PostMapping("/{follow_id}/accept")
	@Operation(summary = "팔로우 요청 수락", description = "특정 팔로우 요청을 수락합니다.")
	public CustomResponseBody<FollowResponse> acceptFollowRequest(@PathVariable("follow_id") Long followId, Principal principal) {
		Long memberId = Long.parseLong(principal.getName());
		FollowResponse result = followService.acceptFollowRequest(memberId, followId);
		return CustomResponseBody.of("팔로우 요청 수락이 성공적으로 완료되었습니다.", result);
	}
}
