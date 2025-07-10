package com.mysite.sns1_server.domain.chat.document;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Document(collection = "chats")
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Chat {
	@Id
	private String id;

	@Indexed
	private Long chatRoomId;

	private Long senderId; // 보낸 사람 ID

	private String content; // 채팅

	@CreatedDate
	private Instant createdAt; // 생성 시간
}
