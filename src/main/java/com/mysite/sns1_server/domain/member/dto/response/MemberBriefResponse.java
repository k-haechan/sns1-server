package com.mysite.sns1_server.domain.member.dto.response;

import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberBriefResponse(
	Long memberId,
	String username,
	String realName,
	String profileImageUrl
) {

	public static MemberBriefResponse from(Member member) {
		return new MemberBriefResponse(
			member.getId(),
			member.getUsername(),
			member.getRealName(),
			member.getProfileImageUrl()
		);
	}
}
