package com.mysite.sns1_server.domain.member.dto.response;

import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberResponse(
	Long memberId,
	String username
) {
	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getUsername());
	}
}
