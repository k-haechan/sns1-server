package com.mysite.sns1_server.domain.member.dto;

import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberResponse(
	Long id,
	String username,
	String profileImageUrl
) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(
			member.getId(),
			member.getUsername(),
			member.getProfileImageUrl()
		);
	}
}
