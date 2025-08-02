package com.mysite.sns1_server.domain.notification.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.notification.entity.Notification;

public record NotificationResponse(
	// Notification ID
	@JsonProperty("notification_id")
	Long notificationId,
	// Notification 타입
	String type,
	// type에서 사용하는 ID
	@JsonProperty("sub_id")
	Long subId,

	// Notification 메시지
	String message,
	// Notification 읽음 여부
	@JsonProperty("is_read")
	Boolean isRead,
	// Notification 생성 시간
	@JsonProperty("created_at")
	Instant createdAt
) {
	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
			notification.getId(),
			notification.getType().name(),
			notification.getSubId(),
			notification.getMessage(),
			notification.isRead(),
			notification.getCreatedAt()
		);
	}
}
