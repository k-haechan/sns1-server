package com.mysite.sns1_server.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

	Optional<ChatRoom> findByLowerMemberIdAndUpperMemberId(Long lowerMemberId, Long upperMemberId);

	List<ChatRoom> findByLowerMemberIdOrUpperMemberId(Long memberId, Long memberId2);
}
