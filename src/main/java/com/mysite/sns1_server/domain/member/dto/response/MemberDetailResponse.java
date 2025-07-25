package com.mysite.sns1_server.domain.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.member.entity.Member;

public record MemberDetailResponse(
	@JsonProperty("member_id")
	Long memberId,
	String username,
	@JsonProperty("real_name")
	String realName,
	@JsonProperty("profile_image_url")
	String profileImageUrl,
	String introduction,
	@JsonProperty("follower_count")
	Long followerCount,
	@JsonProperty("following_count")
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
