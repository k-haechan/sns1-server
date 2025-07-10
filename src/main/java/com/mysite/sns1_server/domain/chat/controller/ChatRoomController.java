package com.mysite.sns1_server.domain.chat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.chat.entity.ChatRoom;
import com.mysite.sns1_server.domain.chat.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chat.service.ChatRoomService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-room")
public class ChatRoomController {
	private final ChatRoomService chatRoomService;

	@GetMapping
	public CustomResponseBody<ChatRoomResponse> getChatRoom(@RequestParam Long recipientId, Principal principal) {
		Long senderId = Long.parseLong(principal.getName());
		// 채팅방 조회 로직 호출
		ChatRoom chatRoom = chatRoomService.findChatRoom(senderId, recipientId);

		return CustomResponseBody.of("채팅방 조회 성공", ChatRoomResponse.from(chatRoom));
	}

	@GetMapping("/list")
	public CustomResponseBody<List<ChatRoomResponse>> getChatRooms(Principal principal) {
		Long memberId = Long.parseLong(principal.getName());
		List<ChatRoom> chatRooms = chatRoomService.getChatRoomsForMember(memberId);
		List<ChatRoomResponse> chatRoomResponses = chatRooms.stream()
			.map(ChatRoomResponse::from)
			.toList();
		return CustomResponseBody.of("채팅방 목록 조회 성공", chatRoomResponses);
	}
}
