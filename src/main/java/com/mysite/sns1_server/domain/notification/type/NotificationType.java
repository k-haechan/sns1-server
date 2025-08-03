package com.mysite.sns1_server.domain.notification.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationType {
	FOLLOW_REQUEST("%s님이 팔로우 요청 하였습니다."), // 팔로우 요청
	FOLLOW_ACCEPTED("%s님이 팔로우 요청을 수락하였습니다."), // 팔로우 수락
	FOLLOWED("%s님이 팔로우 하였습니다."), // 팔로우됨
	POST_LIKE("%s님이 게시글에 좋아요를 눌렀습니다."), // 게시글 좋아요
	COMMENT("%s님이 댓글을 작성하였습니다."); // 댓글 작성

	private final String messageTemplate;

	public String getMessage(String username) {
		return String.format(messageTemplate, username);
	}
}
