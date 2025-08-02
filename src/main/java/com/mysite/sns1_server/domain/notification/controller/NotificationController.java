package com.mysite.sns1_server.domain.notification.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mysite.sns1_server.domain.notification.component.SseEmitterManager;
import com.mysite.sns1_server.domain.notification.dto.response.NotificationResponse;
import com.mysite.sns1_server.domain.notification.service.NotificationService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

	private final SseEmitterManager sseEmitterManager;
	private final NotificationService notificationService;

	@GetMapping
	@Tag(name = "Notification", description = "알림 내용 확인")
	public CustomResponseBody<Slice<NotificationResponse>> getNotifications(Principal principal,
		@RequestParam (
		value = "last-notification-id",
			required = false) Long lastNotificationId,
		Pageable pageable) {
		Slice<NotificationResponse> notifications = notificationService.getNotifications(principal, lastNotificationId,
			pageable);
		return CustomResponseBody.of("알림 조회가 성공적으로 완료되었습니다.", notifications);
	}

	@GetMapping("/subscribe")
	public SseEmitter subscribe(Principal principal) {
		String memberId = principal.getName();
		return sseEmitterManager.subscribe(memberId);
	}

	@GetMapping("/unsubscribe")
	public ResponseEntity<Map<String, Object>> unsubscribe(Principal principal) {
		String memberId = principal.getName();
		sseEmitterManager.removeEmitter(memberId);
		return ResponseEntity.ok(Map.of("result", "구독이 성공적으로 취소되었습니다."));
	}
}
