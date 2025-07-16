package com.mysite.sns1_server.domain.chatRoom.dto.response;

import java.util.List;

import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
import com.mysite.sns1_server.domain.member.entity.Member;

public record ChatRoomResponse(
	Long chatRoomId,
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
