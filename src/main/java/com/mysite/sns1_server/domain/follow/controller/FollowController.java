package com.mysite.sns1_server.domain.follow.controller;

import java.security.Principal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

	@GetMapping("/members/{member-id}")
	@Operation(summary = "팔로우 관계 확인", description = "특정 회원을 팔로잉 하고 있는지 확인합니다..")
	public CustomResponseBody<String> getFollow(@PathVariable("member-id") Long followerId, Principal principal) {
		String result = followService.isFollow(principal, followerId);

		return CustomResponseBody.of("팔로우 관계 확인이 성공적으로 완료되었습니다.", result);
	}

	@PostMapping("/members/{member-id}")
	@Operation(summary = "팔로우 요청", description = "특정 회원을 팔로우 요청합니다.")
	public CustomResponseBody<FollowResponse> requestFollow(@PathVariable("member-id") Long followerId, Principal principal) {
		FollowResponse result = followService.requestFollow(principal, followerId);

		return CustomResponseBody.of("팔로우 요청이 성공적으로 완료되었습니다.", result);
	}

	@GetMapping
	@Operation(summary = "팔로우 요청 조회", description = "내 팔로우 요청을 조회합니다.")
	public CustomResponseBody<Slice<FollowResponse>> findMyFollow(Principal principal, Pageable pageable) {
		Slice<FollowResponse> result = followService.findMyFollowRequest(principal, pageable);
		return CustomResponseBody.of("팔로우 요청 조회가 성공적으로 완료되었습니다.", result);
	}

	@GetMapping("/one")
	@Operation(summary = "팔로우 요청 단건 조회", description = "내 팔로우 요청을 조회합니다.")
	public CustomResponseBody<FollowResponse> findMyFollowRequest(@RequestParam("following-id") Long followingId, Principal principal) {
		FollowResponse result = followService.findMyFollowRequestWithFollowingId(principal, followingId);
		return CustomResponseBody.of("팔로우 요청 단건 조회가 성공적으로 완료되었습니다.", result);
	}

	@PostMapping("/{follow-id}/accept")
	@Operation(summary = "팔로우 요청 수락", description = "특정 팔로우 요청을 수락합니다.")
	public CustomResponseBody<FollowResponse> acceptFollowRequest(@PathVariable("follow-id") Long followId, Principal principal,
		@RequestParam(value = "notification-id") Long notificationId) {
		FollowResponse result = followService.acceptFollowRequest(principal, followId, notificationId);
		return CustomResponseBody.of("팔로우 요청 수락이 성공적으로 완료되었습니다.", result);
	}

	@PostMapping("/{follow-id}/reject")
	@Operation(summary = "팔로우 요청 거절", description = "특정 팔로우 요청을 거절합니다.")
	public CustomResponseBody<String> rejectFollowRequest(@PathVariable("follow-id") Long followId, Principal principal,
		@RequestParam(value = "notification-id") Long notificationId) {
		String result = followService.rejectFollowRequest(principal, followId, notificationId);
		return CustomResponseBody.of("팔로우 요청 거절이 성공적으로 완료되었습니다.", result);
	}

	@PostMapping("/{follow-id}/cancel")
	@Operation(summary = "팔로우 요청 취소", description = "특정 팔로우 요청을 취소합니다.")
	public CustomResponseBody<String> cancelFollowRequest(@PathVariable("follow-id") Long followId, Principal principal) {
		String result = followService.cancelFollowRequest(principal, followId);
		return CustomResponseBody.of("팔로우 요청 거절이 성공적으로 완료되었습니다.", result);
	}

	@DeleteMapping("/members/{member-id}")
	@Operation(summary = "팔로우 취소", description = "특정 회원과 팔로우를 취소합니다.")
	public CustomResponseBody<String> cancelFollow(@PathVariable("member-id") Long memberId, Principal principal) {
		String result = followService.unFollow(principal, memberId);
		return CustomResponseBody.of("팔로우 취소가 성공적으로 완료되었습니다.", result);
	}

	@GetMapping("/members/{member-id}/followers")
	@Operation(summary = "팔로워 조회", description = "특정 회원의 팔로워를 조회합니다.")
	public CustomResponseBody<Slice<FollowResponse>> getFollowers(@PathVariable("member-id") Long memberId, Principal principal, Pageable pageable) {
		Slice<FollowResponse> result = followService.getFollower(principal, memberId, pageable);
		return CustomResponseBody.of("팔로워 조회가 성공적으로 완료되었습니다.", result);
	}

	@GetMapping("/members/{member-id}/followings")
	@Operation(summary = "팔로잉 조회", description = "특정 회원의 팔로잉을 조회합니다.")
	public CustomResponseBody<Slice<FollowResponse>> getFollowings(@PathVariable("member-id") Long memberId, Principal principal, Pageable pageable) {
		Slice<FollowResponse> result = followService.getFollowing(principal, memberId, pageable);
		return CustomResponseBody.of("팔로잉 조회가 성공적으로 완료되었습니다.", result);
	}
}
