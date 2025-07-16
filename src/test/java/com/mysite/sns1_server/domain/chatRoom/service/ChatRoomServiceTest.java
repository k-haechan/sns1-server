
package com.mysite.sns1_server.domain.chatRoom.service;

import com.mysite.sns1_server.domain.chatRoom.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoom;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomMemberRepository;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomRepository;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

@DisplayName("ChatRoomService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @DisplayName("getChatRoomForMemberIds: 기존 채팅방이 존재할 경우 해당 채팅방 반환")
    @Test
    void t1() {
        // given
        Long memberId1 = 1L;
        Long memberId2 = 2L;
        List<Long> memberIds = List.of(memberId1, memberId2);
        Member member1 = mock(Member.class);
        when(member1.getId()).thenReturn(memberId1);
        Member member2 = mock(Member.class);
        when(member2.getId()).thenReturn(memberId2);
        ChatRoom chatRoom = mock(ChatRoom.class);
        when(chatRoom.getId()).thenReturn(1L);

        when(memberRepository.findById(memberId1)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(memberId2)).thenReturn(Optional.of(member2));
        when(chatRoomMemberRepository.findChatRoomByMemberIds(memberIds)).thenReturn(Optional.of(chatRoom));

        // when
        ChatRoomResponse result = chatRoomService.getChatRoomForMemberIds(memberIds);

        // then
        assertThat(result.chatRoomId()).isEqualTo(chatRoom.getId());
    }

    @DisplayName("getChatRoomForMemberIds: 기존 채팅방이 없을 경우 새로운 채팅방 생성")
    @Test
    void t2() {
        // given
        Long memberId1 = 1L;
        Long memberId2 = 2L;
        List<Long> memberIds = List.of(memberId1, memberId2);
        Member member1 = mock(Member.class);
        when(member1.getId()).thenReturn(memberId1);
        Member member2 = mock(Member.class);
        when(member2.getId()).thenReturn(memberId2);
        ChatRoom newChatRoom = mock(ChatRoom.class);
        when(newChatRoom.getId()).thenReturn(1L);

        when(memberRepository.findById(memberId1)).thenReturn(Optional.of(member1));
        when(memberRepository.findById(memberId2)).thenReturn(Optional.of(member2));
        when(chatRoomMemberRepository.findChatRoomByMemberIds(memberIds)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(newChatRoom);

        // when
        ChatRoomResponse result = chatRoomService.getChatRoomForMemberIds(memberIds);

        // then
        assertThat(result.chatRoomId()).isEqualTo(newChatRoom.getId());
    }

    @DisplayName("findChatRoomsByMemberId: 채팅방 목록 조회 성공")
    @Test
    void t3() {
        // given
        Long memberId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);
        List<ChatRoomResponse> chatRoomResponses = List.of(new ChatRoomResponse(1L, null, null));
        Slice<ChatRoomResponse> expectedSlice = new SliceImpl<>(chatRoomResponses, pageable, false);

        when(chatRoomMemberRepository.findChatRoomsByMemberId(memberId, pageable)).thenReturn(expectedSlice);

        // when
        Slice<ChatRoomResponse> result = chatRoomService.findChatRoomsByMemberId(memberId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).chatRoomId()).isEqualTo(1L);
    }

    @DisplayName("checkAuthenticationToChatRoom: 채팅방 멤버 인증 성공")
    @Test
    void t4() {
        // given
        Long memberId = 1L;
        Long chatRoomId = 1L;
        when(chatRoomMemberRepository.existsByChatRoomIdAndMemberId(memberId, chatRoomId)).thenReturn(true);

        // when, then
        chatRoomService.checkAuthenticationToChatRoom(memberId, chatRoomId);
        // No exception should be thrown
    }

    @DisplayName("checkAuthenticationToChatRoom: 채팅방 멤버 인증 실패 - 멤버를 찾을 수 없음")
    @Test
    void t5() {
        // given
        Long memberId = 1L;
        Long chatRoomId = 1L;
        when(chatRoomMemberRepository.existsByChatRoomIdAndMemberId(memberId, chatRoomId)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> chatRoomService.checkAuthenticationToChatRoom(memberId, chatRoomId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_ROOM_MEMBER_NOT_FOUND);
    }
}
