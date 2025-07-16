package com.mysite.sns1_server.domain.chat.dto.request;

import com.mysite.sns1_server.domain.chat.document.Chat;

public record ChatRequest(
	Long chatRoomId,
	Long senderId,
	String senderRealName,
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
