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

@Entity
public class Follow extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_id", nullable = false)
	private Member requester; // 팔로우 요청을 보낸 회원

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_id", nullable = false)
	private Member target; // 팔로우 요청을 받은 회원

	@Enumerated(EnumType.STRING)
	private FollowStatus status; // 팔로우 상태 (요청, 수락, 거절 등)
}
