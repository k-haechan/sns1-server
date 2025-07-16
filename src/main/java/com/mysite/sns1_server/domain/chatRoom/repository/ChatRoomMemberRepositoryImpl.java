package com.mysite.sns1_server.domain.chatRoom.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.mysite.sns1_server.domain.chatRoom.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;
import com.mysite.sns1_server.domain.chatRoom.entity.QChatRoomMember;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
import com.mysite.sns1_server.domain.member.entity.QMember;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatRoomMemberRepositoryImpl implements ChatRoomMemberRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<ChatRoomResponse> findChatRoomsByMemberId(Long memberId, Pageable pageable) {
		QChatRoomMember crm = QChatRoomMember.chatRoomMember;
		QMember member = QMember.member;

		// 1. chatRoom 조회 (페이징과 정렬 적용, +1개 더 조회)
		List<ChatRoom> chatRooms = queryFactory
			.select(crm.chatRoom)
			.from(crm)
			.where(crm.member.id.eq(memberId))
			.orderBy(crm.chatRoom.updatedAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1) // limit + 1 전략
			.fetch();

		boolean hasNext = false;
		if (chatRooms.size() > pageable.getPageSize()) {
			hasNext = true;
			chatRooms.remove(chatRooms.size() - 1); // 초과된 1개 제거
		}

		if (chatRooms.isEmpty()) {
			return new SliceImpl<>(Collections.emptyList(), pageable, hasNext);
		}

		List<Long> chatRoomIds = chatRooms.stream()
			.map(ChatRoom::getId)
			.toList();

		// 2. chatRoom 멤버 조회
		List<Tuple> tuples = queryFactory
			.select(crm.chatRoom.id, member)
			.from(crm)
			.join(crm.member, member)
			.where(crm.chatRoom.id.in(chatRoomIds))
			.fetch();

		// 3. 그룹핑
		Map<Long, List<MemberBriefResponse>> grouped = tuples.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(crm.chatRoom.id),
				Collectors.mapping(
					tuple -> MemberBriefResponse.from(tuple.get(member)),
					Collectors.toList()
				)
			));

		// 4. ChatRoomResponse 리스트 생성
		List<ChatRoomResponse> content = chatRooms.stream()
			.map(room -> new ChatRoomResponse(
				room.getId(),
				room.getLastChat(),
				grouped.getOrDefault(room.getId(), Collections.emptyList())
			))
			.toList();

		// 5. SliceImpl 반환 (count 쿼리 없음)
		return new SliceImpl<>(content, pageable, hasNext);
	}

	@Override
	public Optional<ChatRoom> findChatRoomByMemberIds(List<Long> memberIds) {
		QChatRoomMember crm = QChatRoomMember.chatRoomMember;

		// 채팅방 ID를 찾기 위한 쿼리
		ChatRoom chatRoom = queryFactory
			.select(crm.chatRoom)
			.from(crm)
			.where(crm.member.id.in(memberIds))
			.groupBy(crm.chatRoom.id)
			.having(crm.count().eq((long) memberIds.size()))
			.fetchOne();

		return Optional.ofNullable(chatRoom);
	}
}
