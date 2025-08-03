package com.mysite.sns1_server.domain.notification.component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {

	// 사용자별 SseEmitter 저장
	private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

	// 사용자별 SseEmitter를 추가하는 메서드
	private SseEmitter createEmitter(String memberId) {
		SseEmitter emitter = new SseEmitter(0L);  // 타임아웃 60초

		// 타임아웃이나 연결 종료 시 Emitter 제거
		emitter.onTimeout(() -> removeEmitter(memberId));
		emitter.onCompletion(() -> removeEmitter(memberId));

		emitterMap.put(memberId, emitter);
		return emitter;
	}

	// 사용자별 SseEmitter를 제거하는 메서드
	public void removeEmitter(String memberId) {
		emitterMap.remove(memberId);
	}

	// 사용자별 SseEmitter 가져오기
	public Optional<SseEmitter> getEmitter(String memberId) {
		return Optional.ofNullable(emitterMap.get(memberId));
	}

	// 클라이언트의 SSE 구독 요청 처리 메서드
	public SseEmitter subscribe(String memberId) {
		return getEmitter(memberId).orElseGet(
			() -> createEmitter(memberId)  // Emitter가 없으면 새로 생성
		);
	}
}
