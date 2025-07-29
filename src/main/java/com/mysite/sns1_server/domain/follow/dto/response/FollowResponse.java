package com.mysite.sns1_server.domain.follow.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.follow.entity.Follow;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;

public record FollowResponse(
	@JsonProperty("follow_id")
	Long followId, // 팔로우 ID
	String status,
	MemberBriefResponse follower, // 팔로우 요청을 받은 회원
	MemberBriefResponse following // 팔로우 요청을 보낸 회원
) {
	public static FollowResponse from(Follow follow) {
		return new FollowResponse(
			follow.getId(),
			follow.getStatus().name(),
			com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse.from(follow.getFollower()),
			com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse.from(follow.getFollowing())
		);
	}
}
