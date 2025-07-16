package com.mysite.sns1_server.domain.chatRoom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoomMember;
import com.mysite.sns1_server.domain.member.entity.Member;

import io.lettuce.core.dynamic.annotation.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember,Long>, ChatRoomMemberRepositoryCustom {
	List<ChatRoomMember> findByMember(Member member);

	@Query("select crm.chatRoom.id from ChatRoomMember crm where crm.member = :member")
	List<Long> findChatRoomIdsByMember(@Param("member") Member member);


	@Query("SELECT crm FROM ChatRoomMember crm WHERE crm.chatRoom.id = :chatRoomId AND crm.member.id = :memberId")
	Optional<ChatRoomMember> findByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ChatRoomMember c WHERE c.chatRoom.id = :chatRoomId AND c.member.id = :memberId")
	boolean existsByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId, @Param("memberId") Long memberId);}
