package com.mysite.sns1_server.domain.follow.type;

import lombok.Getter;

@Getter
public enum FollowStatus {
	REQUESTED("REQUESTED"),   // 팔로우 요청 상태
	ACCEPTED("ACCEPTED"),   // 팔로우 수락 상태
	REJECTED("REJECTED");   // 팔로우 거절 상태

	private final String value;

	FollowStatus(String value) {
		this.value = value;
	}

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
