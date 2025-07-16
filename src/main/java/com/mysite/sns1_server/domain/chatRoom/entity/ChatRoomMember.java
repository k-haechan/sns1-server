package com.mysite.sns1_server.domain.chatRoom.entity;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseEntity {
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	private ChatRoom chatRoom;

	protected ChatRoomMember(ChatRoom chatRoom, Member member) {
		this.member = member;
		this.chatRoom = chatRoom;
	}

	public static ChatRoomMember createChatRoomMember(ChatRoom chatRoom, Member member) {
		return new ChatRoomMember(chatRoom, member);
	}
}
