package com.mysite.sns1_server.domain.chat.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.mysite.sns1_server.domain.chat.dto.request.ChatRequest;
import com.mysite.sns1_server.domain.chat.service.ChatService;
import com.mysite.sns1_server.domain.chatRoom.service.ChatRoomService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;
	private final ChatRoomService chatRoomService;


	@MessageMapping("/chat.send") // 클라이언트에서 /app/chat.send로 보냄
	@Operation(summary = "채팅 전송", description = "채팅방에 채팅을 전송합니다.")
	public void sendPrivateMessage(@Payload ChatRequest message, Principal principal) {
		Long senderId = Long.parseLong(principal.getName());
		Long chatRoomId = message.chatRoomId();

		message = message.addSenderId(senderId);

		// 채팅방 권한 확인
		chatRoomService.checkAuthenticationToChatRoom(chatRoomId, senderId);
		// 메시지 저장 및 전송
		chatService.sendPrivateMessage(message, chatRoomId);
	}
}
