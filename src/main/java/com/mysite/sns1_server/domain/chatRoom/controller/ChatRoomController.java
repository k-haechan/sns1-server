package com.mysite.sns1_server.domain.chatRoom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.chat.document.Chat;
import com.mysite.sns1_server.domain.chat.dto.response.ChatResponse;
import com.mysite.sns1_server.domain.chat.service.ChatService;
import com.mysite.sns1_server.domain.chatRoom.dto.request.ChatRoomRequest;
import com.mysite.sns1_server.domain.chatRoom.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chatRoom.service.ChatRoomService;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat-rooms")
@Tag(name = "Chat", description = "채팅 관련 API")
public class ChatRoomController {
	private final ChatRoomService chatRoomService;
	private final ChatService chatService;

	@PostMapping
	@Operation(summary = "채팅방 요청", description = "특정 회원과의 채팅방을 요청합니다. 채팅방이 없다면 새로 생성합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<ChatRoomResponse> getChatRoom(@RequestBody ChatRoomRequest request, Principal principal) {
		Long senderId = Long.parseLong(principal.getName());
		Long recipientId = request.recipientId();

		ChatRoomResponse result = chatRoomService
			.getChatRoomForMemberIds(List.of(senderId, recipientId));

		return CustomResponseBody.of("채팅방 요청 성공", result);
	}

	@GetMapping
	@Operation(summary = "채팅방 목록 조회", description = "로그인한 사용자의 모든 채팅방 목록을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Slice<ChatRoomResponse>> getChatRoomsByMember(Principal principal, Pageable pageable) {
		Long memberId = Long.parseLong(principal.getName());
		Slice<ChatRoomResponse> result = chatRoomService.findChatRoomsByMemberId(memberId, pageable);

		return CustomResponseBody.of("채팅방 목록 조회 성공", result);
	}

	// 채팅 내용 조회
	@GetMapping("/{chat_room_id}/messages")
	@Operation(summary = "채팅 조회", description = "채팅방의 채팅을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<Slice<ChatResponse>> getChatMessages(
		Principal principal,
		@PathVariable("chat_room_id") Long chatRoomId,
		@RequestParam(name = "last_chat_id",required = false) String lastChatId) {

		Long senderId = Long.parseLong(principal.getName());
		Slice<Chat> chatMessages = chatService.getChatMessages(senderId, chatRoomId, lastChatId);
		Slice<ChatResponse> result = chatMessages.map(ChatResponse::from);

		return CustomResponseBody.of("채팅 조회 성공", result);
	}
}
