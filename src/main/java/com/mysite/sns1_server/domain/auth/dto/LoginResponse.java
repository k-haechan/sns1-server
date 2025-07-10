package com.mysite.sns1_server.domain.auth.dto;

import com.mysite.sns1_server.domain.member.entity.Member;

public record LoginResponse(
	Long memberId,
	String username,
	String profileImageUrl,
	String realName
) {
	public static LoginResponse from (Member member) {
		return new LoginResponse(
			member.getId(),
			member.getUsername(),
			member.getProfileImageUrl(),
			member.getRealName()
		);
	}
}
