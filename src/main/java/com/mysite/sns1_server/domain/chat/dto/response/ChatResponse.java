package com.mysite.sns1_server.domain.chat.dto.response;

import com.mysite.sns1_server.domain.chat.document.Chat;

public record ChatResponse(
	String chatId,
	Long chatRoomId,
	Long senderId,
	String senderUsername,
	String senderRealName,
	String content,
	String createdAt
) {
	public static ChatResponse from(Chat chat) {
		return new ChatResponse(
			chat.getId(),
			chat.getChatRoomId(),
			chat.getSenderId(),
			chat.getSenderUsername(),
			chat.getSenderRealName(),
			chat.getContent(),
			chat.getCreatedAt().toString()
		);
	}
}