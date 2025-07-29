//
// package com.mysite.sns1_server.domain.chat.controller;
//
// import static org.mockito.Mockito.*;
//
// import java.security.Principal;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
//
// import com.mysite.sns1_server.domain.chat.dto.request.ChatRequest;
// import com.mysite.sns1_server.domain.chat.service.ChatService;
// import com.mysite.sns1_server.domain.chatRoom.service.ChatRoomService;
//
// @DisplayName("ChatController 단위 테스트")
// @ExtendWith(MockitoExtension.class)
// class ChatControllerTest {
//
//     @Mock
//     private SimpMessagingTemplate messagingTemplate;
//
//     @Mock
//     private ChatService chatService;
//
//     @Mock
//     private ChatRoomService chatRoomService;
//
//     @InjectMocks
//     private ChatController chatController;
//
//     @DisplayName("sendPrivateMessage: 메시지 전송 요청 성공")
//     @Test
//     void t1() {
//         // given
//         ChatRequest message = new ChatRequest(1L, 1L, "testUser", "테스트 유저", "Hello");
//         Principal principal = () -> "1";
//
//         // when
//         chatController.sendPrivateMessage(message, principal);
//
//         // then
//         verify(chatRoomService).checkAuthenticationToChatRoom(1L, 1L);
//         verify(chatService).sendPrivateMessage(message.addSenderId(1L), 1L);
//     }
// }
