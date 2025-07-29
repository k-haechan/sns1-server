package com.mysite.sns1_server.domain.follow.entity;

import com.mysite.sns1_server.domain.follow.type.FollowStatus;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Follow extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "following_id", nullable = false)
	private Member following; // 팔로우 요청을 보낸 회원

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "follower_id", nullable = false)
	private Member follower; // 팔로우 요청을 받은 회원

	@Setter
	@Enumerated(EnumType.STRING)
	private FollowStatus status; // 팔로우 상태 (요청, 수락, 거절 등)

	public static Follow create(Member following, Member follower) {
		Follow follow = new Follow();
		follow.following = following;
		follow.follower = follower;

		follow.status = FollowStatus.REQUESTED; // 초기 상태는 요청됨
		return follow;
	}
}
