package com.mysite.sns1_server.domain.chat.dto.response;

import com.mysite.sns1_server.domain.chat.entity.ChatRoom;

public record ChatRoomResponse(
	Long roomId
) {
	public static ChatRoomResponse from(ChatRoom chatRoom) {
		return new ChatRoomResponse(chatRoom.getId());
	}
}
