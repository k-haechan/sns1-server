package com.mysite.sns1_server.domain.chatRoom.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
import com.mysite.sns1_server.domain.member.entity.Member;

public record ChatRoomResponse(
	@JsonProperty("chat_room_id")
	Long chatRoomId,
	@JsonProperty("last_chat")
	String lastChat,
	List<MemberBriefResponse> members
) {
	public static ChatRoomResponse from(ChatRoom chatRoom, List<Member> members) {
		return new ChatRoomResponse(
			chatRoom.getId(),
			chatRoom.getLastChat(),
			members.stream()
				.map(MemberBriefResponse::from)
				.toList()
		);
	}
}
