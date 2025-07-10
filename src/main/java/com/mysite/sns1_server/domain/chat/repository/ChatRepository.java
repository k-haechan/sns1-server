package com.mysite.sns1_server.domain.chat.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mysite.sns1_server.domain.chat.document.Chat;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

	Page<Chat> findByChatRoomIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long chatRoomId, Instant createdAt, Pageable pageable);

	Page<Chat> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
}
