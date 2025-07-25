package com.mysite.sns1_server.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.chat.document.Chat;

public record ChatResponse(
	@JsonProperty("chat_id")
	String chatId,
	@JsonProperty("chat_room_id")
	Long chatRoomId,
	@JsonProperty("sender_id")
	Long senderId,
	@JsonProperty("sender_username")
	String senderUsername,
	@JsonProperty("sender_real_name")
	String senderRealName,
	String content,
	@JsonProperty("created_at")
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
