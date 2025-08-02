package com.mysite.sns1_server.domain.notification.service;

import java.security.Principal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.notification.component.SseEmitterManager;
import com.mysite.sns1_server.domain.notification.dto.response.NotificationResponse;
import com.mysite.sns1_server.domain.notification.entity.Notification;
import com.mysite.sns1_server.domain.notification.repository.NotificationRepository;
import com.mysite.sns1_server.domain.notification.type.NotificationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final SseEmitterManager sseEmitterManager;

	// 알림 생성 메서드
	public NotificationResponse createNotification(Member member, NotificationType type, String subUsername, Long subId) {
		// 알림 엔티티 생성 및 저장
		Notification notification = Notification.create(member, type, subUsername, subId);

		// SSE를 통해 클라이언트에게 알림 전송
		sseEmitterManager.getEmitter(String.valueOf(member.getId()))
			.ifPresent(emitter -> {
				try {
					// SSE를 통해 클라이언트에게 알림 전송
					emitter.send(notification);
				} catch (Exception e) {
					// 예외 처리 로직 (예: 로그 기록)
					e.printStackTrace();
				}
			});

		Notification save = notificationRepository.save(notification);

		return NotificationResponse.from(save);

	}

	@Transactional(rollbackFor = Exception.class)
	public Slice<NotificationResponse> getNotifications(Principal principal, Long lastNotificationId, Pageable pageable) {
		Long memberId = Long.parseLong(principal.getName());
		Member member = Member.createActor(memberId);

		Slice<Notification> notifications;

		// 마지막 알림 ID가 주어지면 해당 ID 이후의 알림을 가져옴
		if (lastNotificationId != null) {
			notifications = notificationRepository.findByMemberAndIdLessThanOrderByIdDesc(member, lastNotificationId, pageable);
		} else {
			// 마지막 알림 ID가 없으면 최신 알림부터 가져옴
			notifications = notificationRepository.findByMemberOrderByIdDesc(member, pageable);
		}

		// DTO로 먼저 변환하여 반환할 데이터를 준비
		Slice<NotificationResponse> response = notifications.map(NotificationResponse::from);

		for (Notification notification : notifications) {
			// 알림을 읽음 상태로 변경
			notification.markAsRead();
		}

		return response; // 미리 생성해둔 DTO를 반환
	}

}
