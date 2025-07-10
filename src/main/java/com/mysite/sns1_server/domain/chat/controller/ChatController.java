package com.mysite.sns1_server.domain.chat.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysite.sns1_server.domain.chat.document.Chat;
import com.mysite.sns1_server.domain.chat.dto.ChatMessage;
import com.mysite.sns1_server.domain.chat.service.ChatService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;

	@MessageMapping("/chat.send") // 클라이언트에서 /app/chat.send로 보냄
	public void sendPrivateMessage(@Payload ChatMessage message, Principal principal) {
		// 현재 인증된 사용자를 보낸 사람으로 설정
		message.addSender(Long.parseLong(principal.getName()));

		chatService.sendPrivateMessage(message);
	}

	@GetMapping("/chat/room/{chatRoomId}/messages")
	@ResponseBody
	public CustomResponseBody<Page<Chat>> getChatMessages(
		@PathVariable Long chatRoomId,
		@RequestParam(required = false) Instant lastMessageTime,
		@RequestParam(defaultValue = "20") int size
	) {
		Page<Chat> messages = chatService.getChatMessages(chatRoomId, lastMessageTime, size);
		return CustomResponseBody.of("채팅 메시지 조회 성공", messages);
	}
}
