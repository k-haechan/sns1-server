package com.mysite.sns1_server.domain.follow.type;

import lombok.Getter;

@Getter
public enum FollowStatus {
	REQUESTED,   // 팔로우 요청 상태
	ACCEPTED,   // 팔로우 수락 상태
	REJECTED;   // 팔로우 거절 상태


	public boolean isRequested() {
		return this == REQUESTED;
	}

	public boolean isAccepted() {
		return this == ACCEPTED;
	}

	public boolean isRejected() {
		return this == REJECTED;
	}
}
