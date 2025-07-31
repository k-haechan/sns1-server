package com.mysite.sns1_server.domain.follow.service;

import java.security.Principal;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.follow.dto.response.FollowResponse;
import com.mysite.sns1_server.domain.follow.entity.Follow;
import com.mysite.sns1_server.domain.follow.repository.FollowRepository;
import com.mysite.sns1_server.domain.follow.type.FollowStatus;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

	private final MemberRepository memberRepository;
	private final FollowRepository followRepository;

	// 팔로우 요청
	@Transactional
	public FollowResponse requestFollow(Principal principal, Long followerId) {
		// 로그인한 회원이 팔로잉(팔로우 요청을 보내는 사람)
		Long followingId = Long.parseLong(principal.getName());
		Member following = memberRepository.findById(followingId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Following member not found with ID: " + followingId)
		);

		Member follower = memberRepository.findById(followerId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Follower member not found with ID: " + followerId)
		);

		// 팔로우 관계 확인
		followRepository.findFollowByFollowerAndFollowing(follower, following)
			.ifPresent(follow -> { // 이미 팔로우 관계가 존재하는 경우
				if (follow.getStatus() == FollowStatus.ACCEPTED) { // 이미 팔로우 중인 경우
					throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
				} else if (follow.getStatus() == FollowStatus.REQUESTED) { // 이미 팔로우 요청 중인 경우
					throw new CustomException(ErrorCode.ALREADY_REQUESTED);
				}
			});

		Follow follow = Follow.create(following, follower);

		if (Boolean.TRUE.equals(follower.getIsSecret())) {
			follow.setStatus(FollowStatus.REQUESTED); // 비공개 계정인 경우 팔로우 요청 상태로 설정
		} else {
			follow.setStatus(FollowStatus.ACCEPTED);
			following.addFollowing();
			follower.addFollower();
		}
		Follow savedFollow = followRepository.save(follow);

		return FollowResponse.from(savedFollow);
	}

	// 팔로우 요청 조회
	public Slice<FollowResponse> findMyFollowRequest(Principal principal, Pageable pageable) {
		Long memberId = Long.parseLong(principal.getName());
		Member actor = Member.createActor(memberId);
		return followRepository.findByFollowerAndStatus(actor, FollowStatus.REQUESTED, pageable)
			.map(FollowResponse::from);
	}

	// 팔로우 요청 수락
	@Transactional
	public FollowResponse acceptFollowRequest(Principal principal, Long followId) {
		Follow follow = followRepository.findById(followId)
			.orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

		// 로그인한 회원이 팔로우 요청을 수락하는 사람
		Long followerId = Long.parseLong(principal.getName());
		if (!Objects.equals(follow.getFollower().getId(), followerId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		// 팔로우 요청이 이미 수락된 상태인 경우
		if (follow.getStatus() == FollowStatus.ACCEPTED) {
			throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
		}

		follow.setStatus(FollowStatus.ACCEPTED);
		follow.getFollowing().addFollowing();
		follow.getFollower().addFollower();

		return FollowResponse.from(follow);
	}

	// 팔로워 조회(memberId를 팔로우 하는 계정, memberId가 팔로워인 경우)
	// 팔로워 조회는 나 자신 또는 공개 계정만 가능
	public Slice<FollowResponse> getFollower(Principal principal, Long memberId, Pageable pageable) {
		// 팔로워 조회는 나 자신 또는 공개 계정만 가능
		// 1. 권한 확인
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// 비밀 계정인 경우 본인이 아니라면 403 Forbidden
		if (Boolean.TRUE.equals(member.getIsSecret())) {
			//
			if (!Objects.equals(Long.parseLong(principal.getName()), memberId)) {
				throw new CustomException(ErrorCode.FORBIDDEN);
			}
		}

		// 2. 팔로워 조회(memberId가 팔로워인 경우, member를 팔로잉하는 사람들)
		Slice<Follow> followers = followRepository.findByFollowerAndStatus(member, FollowStatus.ACCEPTED,
			pageable);

		return followers.map(FollowResponse::from);
	}

	// 팔로잉 조회(memberId가 팔로우 하는 계정, memberId가 팔로워인 경우)
	public Slice<FollowResponse> getFollowing(Principal principal, Long memberId, Pageable pageable) {
		// 팔로잉 조회는 나 자신 또는 공개 계정만 가능
		// 1. 권한 확인
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		// 비밀 계정인 경우 본인이 아니라면 403 Forbidden
		if (Boolean.TRUE.equals(member.getIsSecret())) {
			if (!Objects.equals(Long.parseLong(principal.getName()), memberId)) {
				throw new CustomException(ErrorCode.FORBIDDEN);
			}
		}

		// 2. 팔로잉 조회(memberId가 팔로우 하는 계정들)
		Slice<Follow> followings = followRepository.findByFollowingAndStatus(member, FollowStatus.ACCEPTED,
			pageable);

		return followings.map(FollowResponse::from);
	}

	// 팔로우 관계 확인(로그인한 회원이 followerId를 팔로우 하고 있는지 확인)
	public String isFollow(Principal principal, Long followerId) {
		Long followingId = Long.parseLong(principal.getName());
		Member following = memberRepository.findById(followingId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Following member not found with ID: " + followingId)
		);

		Member follower = memberRepository.findById(followerId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Follower member not found with ID: " + followerId)
		);

		FollowStatus followStatus = followRepository.findStatusByFollowerAndFollowing(follower, following);
		if( followStatus == null) {
			// 팔로우 관계가 없는 경우
			return "NONE";
		} else {
			// 팔로우 관계가 있는 경우
			return followStatus.name();
		}
	}

	public String rejectFollowRequest(Principal principal, Long followId) {
		Follow follow = followRepository.findById(followId)
			.orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

		// 로그인한 회원이 팔로우 요청을 거절하는 사람
		Long followerId = Long.parseLong(principal.getName());
		if (!Objects.equals(follow.getFollower().getId(), followerId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		if (follow.getStatus() != FollowStatus.REQUESTED) {
			throw new CustomException(ErrorCode.INVALID_FOLLOW_STATUS);
		}

		followRepository.delete(follow);
		return "NONE";
	}

	@Transactional
	public String cancelFollow(Principal principal, Long followerId) {
		Long followingId = Long.parseLong(principal.getName());

		Member following = Member.createActor(followingId);
		Member follower  = Member.createActor(followerId);

		Follow follow = followRepository.findFollowByFollowerAndFollowing(follower,
			following).orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

		follow.setStatus(FollowStatus.NONE);

		follow.getFollowing().removeFollowing();
		follow.getFollower().removeFollower();

		followRepository.delete(follow);

		return "NONE";
	}
}
