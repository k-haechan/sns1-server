package com.mysite.sns1_server.domain.chatRoom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
	@Modifying
	@Query("UPDATE ChatRoom c SET c.lastChat = :lastChat, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :chatRoomId")
	void updateLastChat(@Param("chatRoomId") Long chatRoomId, @Param("lastChat") String lastChat);}
