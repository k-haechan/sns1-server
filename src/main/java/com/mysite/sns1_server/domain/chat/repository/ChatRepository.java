package com.mysite.sns1_server.domain.chat.repository;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mysite.sns1_server.domain.chat.document.Chat;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {
	Slice<Chat> findByChatRoomIdAndCreatedAtGreaterThanEqualAndIdLessThanOrderByIdDesc(
		Long chatRoomId,
		Instant joinedAt,
		String lastChatId,
		Pageable pageable
	);

	Slice<Chat> findByChatRoomIdAndCreatedAtGreaterThanEqualOrderByIdDesc(
		Long chatRoomId,
		Instant joinedAt,
		Pageable pageable
	);
}
