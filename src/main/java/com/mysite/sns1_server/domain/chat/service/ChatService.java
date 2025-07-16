package com.mysite.sns1_server.domain.chat.service;

import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.chat.document.Chat;
import com.mysite.sns1_server.domain.chat.dto.request.ChatRequest;
import com.mysite.sns1_server.domain.chat.dto.response.ChatResponse;
import com.mysite.sns1_server.domain.chat.repository.ChatRepository;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoomMember;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomMemberRepository;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomRepository;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
	private static final int CHAT_ROOM_SIZE = 20;

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRepository chatRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatRoomRepository chatRoomRepository;

	private Chat saveMessage(ChatRequest chatRequest) {
		Chat chat = chatRequest.toChat();

		String lastChat = chatRequest.content();
		if (lastChat.length() > 255) {
			lastChat = lastChat.substring(0, 255);
		}

		chatRoomRepository.updateLastChat(chatRequest.chatRoomId(), lastChat);

		return chatRepository.save(chat);
	}
	@Transactional
	public void sendPrivateMessage(ChatRequest message, Long chatRoomId) {
		Chat chat = saveMessage(message);
		ChatResponse chatResponse = ChatResponse.from(chat);
		messagingTemplate.convertAndSend( // 메시지 브로커가 사용자에게 메시지를 전송
			"/topic/chat-room/" + chatRoomId,
			chatResponse
		);

	}

	public Slice<Chat> getChatMessages(Long memberId, Long chatRoomId, String lastChatId) {
		ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND));

		Instant joinedAt = chatRoomMember.getCreatedAt();
		Pageable pageable = PageRequest.of(0, CHAT_ROOM_SIZE, Sort.by(Sort.Direction.DESC, "id"));

		if (lastChatId == null) {
			return chatRepository.findByChatRoomIdAndCreatedAtGreaterThanEqualOrderByIdDesc(
				chatRoomId, joinedAt, pageable
			);
		} else {
			return chatRepository.findByChatRoomIdAndCreatedAtGreaterThanEqualAndIdLessThanOrderByIdDesc(
				chatRoomId, joinedAt, lastChatId, pageable
			);
		}
	}

}
