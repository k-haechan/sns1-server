package com.mysite.sns1_server.domain.chat.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mysite.sns1_server.domain.chat.document.Chat;
import com.mysite.sns1_server.domain.chat.dto.ChatMessage;
import com.mysite.sns1_server.domain.chat.repository.ChatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRepository chatRepository;

	public void saveMessage(ChatMessage message) {
		Chat chat = message.toDocument();
		chatRepository.save(chat);
	}

	public void sendPrivateMessage(ChatMessage message) {
		messagingTemplate.convertAndSendToUser( // 메시지 브로커가 사용자에게 메시지를 전송
			String.valueOf(message.recipientId()), // 수신자 ID
			"/queue/messages",   // /user/수신자ID/queue/messages 로 전송됨
			message
		);

	}

	public Page<Chat> getChatMessages(Long chatRoomId, Instant lastMessageTime, int size) {
		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));

		if (lastMessageTime == null) {
			// lastMessageTime이 없으면 최신 메시지부터 조회
			return chatRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
		} else {
			// lastMessageTime이 있으면 해당 시간 이전 메시지 조회
			return chatRepository.findByChatRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(chatRoomId, lastMessageTime, pageable);
		}
	}
}
