
package com.mysite.sns1_server.domain.chat.service;

import com.mysite.sns1_server.domain.chat.document.Chat;
import com.mysite.sns1_server.domain.chat.dto.request.ChatRequest;
import com.mysite.sns1_server.domain.chat.repository.ChatRepository;
import com.mysite.sns1_server.domain.chatRoom.entity.ChatRoomMember;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomMemberRepository;
import com.mysite.sns1_server.domain.chatRoom.repository.ChatRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ChatService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatService chatService;

    @DisplayName("sendPrivateMessage: 메시지 저장 및 전송 성공")
    @Test
    void t1() {
        // given
                ChatRequest chatRequest = new ChatRequest(1L, 1L, "testUser", "테스트 유저", "Hello");
        Chat chat = mock(Chat.class);
        when(chat.getCreatedAt()).thenReturn(Instant.now());

        when(chatRepository.save(any(Chat.class))).thenReturn(chat);

        // when
        chatService.sendPrivateMessage(chatRequest, 1L);

        // then
        verify(chatRepository).save(any(Chat.class));
        verify(chatRoomRepository).updateLastChat(anyLong(), anyString());
        verify(messagingTemplate).convertAndSend(anyString(), any(Object.class));
    }

    @DisplayName("getChatMessages: 채팅 메시지 조회 성공")
    @Test
    void t2() {
        // given
        Long memberId = 1L;
        Long chatRoomId = 1L;
        ChatRoomMember chatRoomMember = mock(ChatRoomMember.class);
        when(chatRoomMember.getCreatedAt()).thenReturn(Instant.now());
        Slice<Chat> chats = new SliceImpl<>(Collections.singletonList(mock(Chat.class)));

        when(chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoomId, memberId)).thenReturn(Optional.of(chatRoomMember));
        when(chatRepository.findByChatRoomIdAndCreatedAtGreaterThanEqualOrderByIdDesc(anyLong(), any(Instant.class), any(PageRequest.class))).thenReturn(chats);

        // when
        Slice<Chat> result = chatService.getChatMessages(memberId, chatRoomId, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
