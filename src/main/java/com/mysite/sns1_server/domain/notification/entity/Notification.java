package com.mysite.sns1_server.domain.notification.entity;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.notification.type.NotificationType;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Getter
@Entity
public class Notification extends BaseEntity {

	// 알림의 내용
	private String message;

	// 알림을 받은 회원
	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	// 알림의 상태 (읽음 여부)
	private boolean isRead;

	// 알림의 유형
	@Enumerated(EnumType.STRING)
	private NotificationType type;

	// 서브 ID (예: 게시글 ID, 댓글 ID 등)
	private Long subId;

	// 정적 생성 메서드
	public static Notification create(Member member, NotificationType type, String subUsername, Long subId) {
		Notification notification = new Notification();
		notification.message = type.getMessage(subUsername);
		notification.type = type;
		notification.member = member;
		notification.isRead = false;
		notification.subId = subId;
		return notification;
	}

	// 알림을 읽음 상태로 변경
	public void markAsRead() {
		this.isRead = true;
	}

	public void setTypeToFollowed() {
		type = NotificationType.FOLLOWED;
	}
}
