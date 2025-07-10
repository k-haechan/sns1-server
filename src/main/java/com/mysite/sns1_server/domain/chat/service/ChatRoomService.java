package com.mysite.sns1_server.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mysite.sns1_server.domain.chat.entity.ChatRoom;
import com.mysite.sns1_server.domain.chat.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;

	public ChatRoom findChatRoom(Long senderId, Long recipientId) {
		// id 비교 로직
		Long lowerId = Math.min(senderId, recipientId);
		Long upperId = Math.max(senderId, recipientId);

		// 채팅방 조회
		return chatRoomRepository.findByLowerMemberIdAndUpperMemberId(lowerId, upperId)
			.orElseGet(() -> {
				// 채팅방이 없으면 새로 생성
				ChatRoom newChatRoom = new ChatRoom(lowerId, upperId);
				return chatRoomRepository.save(newChatRoom);
			});
	}

	public List<ChatRoom> getChatRoomsForMember(Long memberId) {
		return chatRoomRepository.findByLowerMemberIdOrUpperMemberId(memberId, memberId);
	}
}
