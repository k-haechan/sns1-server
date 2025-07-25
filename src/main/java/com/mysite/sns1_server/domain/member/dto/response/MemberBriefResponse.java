package com.mysite.sns1_server.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberBriefResponse(
	@JsonProperty("member_id")
	Long memberId,
	String username,
	@JsonProperty("real_name")
	String realName,
	@JsonProperty("profile_image_url")
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
