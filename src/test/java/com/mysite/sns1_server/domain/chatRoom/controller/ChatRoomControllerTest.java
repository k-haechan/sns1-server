package com.mysite.sns1_server.domain.chatRoom.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sns1_server.domain.chat.document.Chat;
import com.mysite.sns1_server.domain.chat.service.ChatService;
import com.mysite.sns1_server.domain.chatRoom.dto.request.ChatRoomRequest;
import com.mysite.sns1_server.domain.chatRoom.dto.response.ChatRoomResponse;
import com.mysite.sns1_server.domain.chatRoom.service.ChatRoomService;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;

@DisplayName("ChatRoomController 단위테스트")
@WebMvcTest(ChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatRoomService chatRoomService;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsManager(
                    User.withUsername("1")
                            .password("password")
                            .roles("USER")
                            .build()
            );
        }
    }

    @DisplayName("getChatRoom: 채팅방 요청 성공")
    @Test
    @WithMockUser(username = "1")
    void t1() throws Exception {
        // given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("1");

        ChatRoomRequest request = new ChatRoomRequest(2L);
        ChatRoomResponse response = new ChatRoomResponse(1L, "Test Last Chat", List.of(new MemberBriefResponse(1L, "testUser1", "테스트 유저1", "profile1.jpg"), new MemberBriefResponse(2L, "testUser2", "테스트 유저2", "profile2.jpg")));
        when(chatRoomService.getChatRoomForMemberIds(anyList())).thenReturn(response);

        // when, then
        mockMvc.perform(post("/api/v1/chat-rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("getChatRoomsByMember: 채팅방 목록 조회 성공")
    @Test
    @WithMockUser(username = "1")
    void t2() throws Exception {
        // given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("1");

        PageRequest pageable = PageRequest.of(0, 10);
        ChatRoomResponse chatRoomResponse = new ChatRoomResponse(1L, "Test Last Chat", List.of(new MemberBriefResponse(1L, "testUser1", "테스트 유저1", "profile1.jpg")));
        Slice<ChatRoomResponse> response = new SliceImpl<>(Collections.singletonList(chatRoomResponse), pageable, false);
        when(chatRoomService.findChatRoomsByMemberId(anyLong(), any(PageRequest.class))).thenReturn(response);

        // when, then
                mockMvc.perform(get("/api/v1/chat-rooms")
                        .principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("getChatMessages: 채팅 메시지 조회 성공")
    @Test
    @WithMockUser(username = "1")
    void t3() throws Exception {
        // given
        Long chatRoomId = 1L;
        String lastChatId = "testChatId";
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("1");

        Chat chat = mock(Chat.class);
        when(chat.getId()).thenReturn("chatId1");
        when(chat.getChatRoomId()).thenReturn(chatRoomId);
        when(chat.getSenderId()).thenReturn(1L);
        when(chat.getContent()).thenReturn("Hello");
        when(chat.getCreatedAt()).thenReturn(Instant.now());

        Slice<Chat> chatMessages = new SliceImpl<>(Collections.singletonList(chat), PageRequest.of(0, 10), false);
        when(chatService.getChatMessages(anyLong(), anyLong(), anyString())).thenReturn(chatMessages);

        // when, then
        mockMvc.perform(get("/api/v1/chat-rooms/{chat_room_id}/messages", chatRoomId)
                        .param("last_chat_id", lastChatId)
                        .principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].chat_room_id").value(chatRoomId))
                .andExpect(jsonPath("$.data.content[0].content").value("Hello"));
    }
}
