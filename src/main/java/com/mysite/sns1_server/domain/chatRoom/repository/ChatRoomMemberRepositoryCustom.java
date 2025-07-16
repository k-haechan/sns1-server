package com.mysite.sns1_server.domain.chatRoom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.mysite.sns1_server.domain.chatRoom.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;

public interface ChatRoomMemberRepositoryCustom {
	Slice<ChatRoomResponse> findChatRoomsByMemberId(Long memberId, Pageable pageable);

	Optional<ChatRoom> findChatRoomByMemberIds(List<Long> memberIds);
}
