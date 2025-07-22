package com.mysite.sns1_server.domain.member.dto.response;

import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberDetailResponse(
	Long memberId,
	String username,
	String realName,
	String profileImageUrl,
	String introduction,
	Long followerCount,
	Long followingCount
) {
	public static MemberDetailResponse from(Member member) {
				return new MemberDetailResponse(
			member.getId(),
			member.getUsername(),
			member.getRealName(),
			member.getProfileImageUrl(),
			member.getIntroduction(),
			member.getFollowerCount(),
			member.getFollowingCount()
		);
	}
}
