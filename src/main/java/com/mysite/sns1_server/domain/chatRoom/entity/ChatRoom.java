package com.mysite.sns1_server.domain.chatRoom.entity;

import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {
	// 255
	@Column(length = 255)
	String lastChat;

	public static ChatRoom createChatRoom() {
		return new ChatRoom();
	}
}
