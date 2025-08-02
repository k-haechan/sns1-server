package com.mysite.sns1_server.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.notification.component.SseEmitterManager;
import com.mysite.sns1_server.domain.notification.dto.response.NotificationResponse;
import com.mysite.sns1_server.domain.notification.entity.Notification;
import com.mysite.sns1_server.domain.notification.repository.NotificationRepository;
import com.mysite.sns1_server.domain.notification.type.NotificationType;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SseEmitterManager sseEmitterManager;

    private Principal createPrincipal(Long memberId) {
        return () -> String.valueOf(memberId);
    }

    private Member createMember(Long id) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Notification createNotification(Long id, Member member, NotificationType type, String subUsername, Long subId, boolean isRead) {
        Notification notification = Notification.create(member, type, subUsername, subId);
        ReflectionTestUtils.setField(notification, "id", id);
        ReflectionTestUtils.setField(notification, "isRead", isRead);
        return notification;
    }

    @Nested
    @DisplayName("알림 생성")
    class CreateNotification {

        @Test
        @DisplayName("성공 - SSE 전송 포함")
        void createNotificationSuccessWithSse() throws IOException {
            // given
            Member member = createMember(1L);
            NotificationType type = NotificationType.FOLLOWED;
            String subUsername = "testUser";
            Long subId = 2L;

            Notification notification = createNotification(1L, member, type, subUsername, subId, false);
            SseEmitter mockSseEmitter = mock(SseEmitter.class);
            ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);

            given(notificationRepository.save(any(Notification.class))).willAnswer(invocation -> {
                Notification savedNotification = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedNotification, "id", 1L);
                return savedNotification;
            });
            given(sseEmitterManager.getEmitter(String.valueOf(member.getId()))).willReturn(Optional.of(mockSseEmitter));

            // when
            NotificationResponse response = notificationService.createNotification(member, type, subUsername, subId);

            // then
            assertThat(response.notificationId()).isEqualTo(notification.getId());
            assertThat(response.message()).isEqualTo(notification.getMessage());
            assertThat(response.isRead()).isFalse(); // 생성 시점에는 false
            verify(notificationRepository).save(any(Notification.class));
            verify(mockSseEmitter).send(notificationCaptor.capture()); // SSE 전송 확인
            assertThat(notificationCaptor.getValue().getId()).isEqualTo(notification.getId());
        }

        @Test
        @DisplayName("성공 - SSE 전송 실패 (Emitter 없음)")
        void createNotificationSuccessNoSseEmitter() {
            // given
            Member member = createMember(1L);
            NotificationType type = NotificationType.FOLLOWED;
            String subUsername = "testUser";
            Long subId = 2L;

            Notification notification = createNotification(1L, member, type, subUsername, subId, false);

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);
            given(sseEmitterManager.getEmitter(String.valueOf(member.getId()))).willReturn(Optional.empty());

            // when
            NotificationResponse response = notificationService.createNotification(member, type, subUsername, subId);

            // then
            assertThat(response.notificationId()).isEqualTo(notification.getId());
            assertThat(response.message()).isEqualTo(notification.getMessage());
            assertThat(response.isRead()).isFalse();
            verify(notificationRepository).save(any(Notification.class));
            verify(sseEmitterManager).getEmitter(String.valueOf(member.getId())); // Emitter 조회 시도 확인
            // mockSseEmitter.send()는 호출되지 않음을 암시적으로 확인 (verify(mockSseEmitter, never()).send(any()))
        }
    }

    @Nested
    @DisplayName("알림 조회")
    class GetNotifications {

        @Test
        @DisplayName("성공 - lastNotificationId 없음 (최신 알림 조회)")
        void getNotificationsSuccessNoLastId() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId);
            Notification noti1 = createNotification(3L, member, NotificationType.FOLLOWED, "userA", 10L, false);
            Notification noti2 = createNotification(2L, member, NotificationType.COMMENT, "userB", 20L, false);
            Notification noti3 = createNotification(1L, member, NotificationType.POST_LIKE, "userC", 30L, false);

            Slice<Notification> notificationsSlice = new SliceImpl<>(List.of(noti1, noti2, noti3), pageable, false);

            given(notificationRepository.findByMemberOrderByIdDesc(any(Member.class), any(Pageable.class))).willReturn(notificationsSlice);

            // when
            Slice<NotificationResponse> response = notificationService.getNotifications(principal, null, pageable);

            // then
            assertThat(response.getContent()).hasSize(3);
            assertThat(response.getContent().get(0).notificationId()).isEqualTo(3L);
            assertThat(response.getContent().get(0).isRead()).isFalse(); // 반환 시점에는 false
            assertThat(noti1.isRead()).isTrue(); // markAsRead 호출 후 true
            assertThat(noti2.isRead()).isTrue();
            assertThat(noti3.isRead()).isTrue();
        }

        @Test
        @DisplayName("성공 - lastNotificationId 있음 (커서 기반 페이징)")
        void getNotificationsSuccessWithLastId() {
            // given
            Long memberId = 1L;
            Long lastNotificationId = 5L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId);
            Notification noti1 = createNotification(4L, member, NotificationType.FOLLOWED, "userD", 40L, false);
            Notification noti2 = createNotification(3L, member, NotificationType.COMMENT, "userE", 50L, false);

            Slice<Notification> notificationsSlice = new SliceImpl<>(List.of(noti1, noti2), pageable, false);

            given(notificationRepository.findByMemberAndIdLessThanOrderByIdDesc(any(Member.class), eq(lastNotificationId), any(Pageable.class))).willReturn(notificationsSlice);

            // when
            Slice<NotificationResponse> response = notificationService.getNotifications(principal, lastNotificationId, pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).notificationId()).isEqualTo(4L);
            assertThat(response.getContent().get(0).isRead()).isFalse(); // 반환 시점에는 false
            assertThat(noti1.isRead()).isTrue(); // markAsRead 호출 후 true
            assertThat(noti2.isRead()).isTrue();
        }

        @Test
        @DisplayName("성공 - 알림이 없는 경우")
        void getNotificationsSuccessNoNotifications() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId);
            Slice<Notification> notificationsSlice = new SliceImpl<>(List.of(), pageable, false);

            given(notificationRepository.findByMemberOrderByIdDesc(any(Member.class), any(Pageable.class))).willReturn(notificationsSlice);

            // when
            Slice<NotificationResponse> response = notificationService.getNotifications(principal, null, pageable);

            // then
            assertThat(response.getContent()).isEmpty();
        }
    }
}
