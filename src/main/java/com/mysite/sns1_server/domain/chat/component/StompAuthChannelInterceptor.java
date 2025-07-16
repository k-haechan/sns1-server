package com.mysite.sns1_server.domain.chat.component;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.mysite.sns1_server.domain.chatRoom.service.ChatRoomService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {
	private final ChatRoomService chatRoomService;

	public StompAuthChannelInterceptor(ChatRoomService chatRoomService) {
		this.chatRoomService = chatRoomService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			try {
				String destination = accessor.getDestination(); // 예: "/topic/chat-room/13"
				Long chatRoomId = Long.parseLong(destination.substring(destination.lastIndexOf("/") + 1));
				Principal principal = accessor.getUser();
				Long memberId = Long.parseLong(principal.getName());

				chatRoomService.checkAuthenticationToChatRoom(chatRoomId, memberId);
			} catch (Exception e) {
				log.warn("WebSocket 구독 인증 실패: {}", e.getMessage());
				return null; // 메시지 무시하고 연결은 유지
			}
		}

		return message;
	}
}
