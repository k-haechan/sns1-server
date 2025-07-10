package com.mysite.sns1_server.domain.member.dto;

import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberInfoResponse(
	Long id,
	String username,
	String profileImageUrl,
	String introduction,
	Long followerCount,
	Long followingCount
) {
	public static MemberInfoResponse from(Member member) {
		return new MemberInfoResponse(
			member.getId(),
			member.getUsername(),
			member.getProfileImageUrl(),
			member.getIntroduction(),
			member.getFollowerCount(),
			member.getFollowingCount()
		);
	}
}
