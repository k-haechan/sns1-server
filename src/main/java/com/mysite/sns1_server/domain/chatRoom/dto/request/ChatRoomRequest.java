package com.mysite.sns1_server.domain.chatRoom.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRoomRequest(
	@JsonProperty("recipient_id")
	Long recipientId
) {
}
