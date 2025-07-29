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
	public FollowResponse requestFollow(Long followingId, Long followerId) {
		Member following = memberRepository.findById(followingId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Following member not found with ID: " + followingId)
		);

		Member follower = memberRepository.findById(followerId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Follower member not found with ID: " + followerId)
		);

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
	public Slice<FollowResponse> findMyFollow(Principal principal, Pageable pageable) {
		Long memberId = Long.parseLong(principal.getName());
		Member actor = Member.createActor(memberId);
		return followRepository.findByFollowerAndStatus(actor, FollowStatus.REQUESTED, pageable)
			.map(FollowResponse::from);
	}

	// 팔로우 요청 수락
	@Transactional
	public FollowResponse acceptFollowRequest(Long followerId, Long followId) {
		Follow follow = followRepository.findById(followId)
			.orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

		if (!Objects.equals(follow.getFollower().getId(), followerId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
		if (follow.getStatus() != FollowStatus.REQUESTED) {
			throw new CustomException(ErrorCode.INVALID_FOLLOW_STATUS);
		}

		follow.setStatus(FollowStatus.ACCEPTED);
		follow.getFollowing().addFollowing();
		follow.getFollower().addFollower();

		return FollowResponse.from(followRepository.save(follow));
	}
}
