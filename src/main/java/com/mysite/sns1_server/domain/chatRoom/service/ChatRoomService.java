package com.mysite.sns1_server.domain.chatRoom.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.chatRoom.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoomMember;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomMemberRepository;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomRepository;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final MemberRepository memberRepository;

	private ChatRoom createChatRoom(List<Member> members) {
		ChatRoom newChatRoom = chatRoomRepository.save(ChatRoom.createChatRoom());
		List<ChatRoomMember> chatRoomMembers = members.stream()
			.map(member -> ChatRoomMember.createChatRoomMember(newChatRoom, member))
			.toList();

		chatRoomMemberRepository.saveAll(chatRoomMembers);
		return newChatRoom;
	}


	@Transactional
	public ChatRoomResponse getChatRoomForMemberIds(List<Long> memberIds) {
		List<Member> members = memberIds.stream()
			.map(id -> memberRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)))
			.toList();

		ChatRoom chatRoom = chatRoomMemberRepository.findChatRoomByMemberIds(memberIds)
			.orElseGet(()-> createChatRoom(members));

		return ChatRoomResponse.from(chatRoom, members);
	}

	public Slice<ChatRoomResponse> findChatRoomsByMemberId(Long memberId, Pageable pageable) {
		return chatRoomMemberRepository.findChatRoomsByMemberId(memberId, pageable);
	}

	public void checkAuthenticationToChatRoom(Long chatRoomId, Long memberId) {
		boolean isExist = chatRoomMemberRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId);

		if(!isExist){
			throw new CustomException(ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND);
		}
	}

}
