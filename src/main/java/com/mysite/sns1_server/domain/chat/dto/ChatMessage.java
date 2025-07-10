package com.mysite.sns1_server.domain.chat.dto;

import com.mysite.sns1_server.domain.chat.document.Chat;

public record ChatMessage(
	String type,
	Long roomId,
	Long senderId,
	Long recipientId,
	String content
) {

	public Chat toDocument() {
		return new Chat(
			null,
			this.roomId,
			this.senderId,
			this.content,
			null
		);
	}

	public enum MessageType {
		CHAT, JOIN, LEAVE // (채팅, 입장, 퇴장)
	}

	public ChatMessage addSender(Long sender) {
		return new ChatMessage(this.type, this.roomId ,sender, this.recipientId, this.content);
	}
}
