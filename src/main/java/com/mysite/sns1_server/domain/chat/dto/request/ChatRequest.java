package com.mysite.sns1_server.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.chat.document.Chat;

public record ChatRequest(
	@JsonProperty("chat_room_id")
	Long chatRoomId,
	@JsonProperty("sender_id")
	Long senderId,
	@JsonProperty("sender_real_name")
	String senderRealName,
	@JsonProperty("sender_username")
	String senderUsername,
	String content
) {

	public Chat toChat() {
		return new Chat(
			null,
			this.chatRoomId,
			this.senderId,
			this.senderRealName,
			this.senderUsername,
			this.content,
			null
		);
	}


	public ChatRequest addSenderId(Long senderId) {
		return new ChatRequest(this.chatRoomId ,senderId, this.senderRealName, this.senderUsername,this.content);
	}
}
