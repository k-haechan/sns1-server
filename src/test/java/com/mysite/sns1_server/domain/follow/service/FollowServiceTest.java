package com.mysite.sns1_server.domain.follow.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.mysite.sns1_server.domain.follow.dto.response.FollowResponse;
import com.mysite.sns1_server.domain.follow.entity.Follow;
import com.mysite.sns1_server.domain.follow.repository.FollowRepository;
import com.mysite.sns1_server.domain.follow.type.FollowStatus;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.domain.notification.service.NotificationService;
import com.mysite.sns1_server.domain.notification.type.NotificationType;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private NotificationService notificationService;

    private Principal createPrincipal(Long memberId) {
        return () -> String.valueOf(memberId);
    }

    private Member createMember(Long id, boolean isSecret) {
        Member member = Member.builder()
            .isSecret(isSecret)
            .followerCount(0L)
            .followingCount(0L)
            .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Follow createFollow(Long id, Member following, Member follower, FollowStatus status) {
        Follow follow = Follow.create(following, follower);
        ReflectionTestUtils.setField(follow, "id", id);
        follow.setStatus(status);
        return follow;
    }

    @Nested
    @DisplayName("팔로우 요청")
    class RequestFollow {

        @Test
        @DisplayName("성공 - 공개 계정")
        void requestFollowSuccessPublic() {
            // given
            Principal principal = createPrincipal(1L);
            Member following = createMember(1L, false);
            Member follower = createMember(2L, false);

            given(memberRepository.findById(1L)).willReturn(Optional.of(following));
            given(memberRepository.findById(2L)).willReturn(Optional.of(follower));
            given(followRepository.save(any(Follow.class))).willAnswer(invocation -> {
                Follow savedFollow = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedFollow, "id", 1L);
                return savedFollow;
            });

            // when
            FollowResponse response = followService.requestFollow(principal, 2L);

            // then
            assertThat(response.status()).isEqualTo(FollowStatus.ACCEPTED.name());
            assertThat(following.getFollowingCount()).isEqualTo(1);
            assertThat(follower.getFollowerCount()).isEqualTo(1);
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("성공 - 비공개 계정")
        void requestFollowSuccessPrivate() {
            // given
            Principal principal = createPrincipal(1L);
            Member following = createMember(1L, false);
            Member follower = createMember(2L, true);

            given(memberRepository.findById(1L)).willReturn(Optional.of(following));
            given(memberRepository.findById(2L)).willReturn(Optional.of(follower));
            given(followRepository.save(any(Follow.class))).willAnswer(invocation -> {
                Follow savedFollow = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedFollow, "id", 1L);
                return savedFollow;
            });

            // when
            FollowResponse response = followService.requestFollow(principal, 2L);

            // then
            assertThat(response.status()).isEqualTo(FollowStatus.REQUESTED.name());
            assertThat(following.getFollowingCount()).isZero();
            assertThat(follower.getFollowerCount()).isZero();
            verify(followRepository).save(any(Follow.class));
        }

        @Test
        @DisplayName("실패 - 팔로우할 회원을 찾을 수 없음")
        void requestFollowFailFollowingNotFound() {
            // given
            Principal principal = createPrincipal(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(createMember(1L, false)));
            given(memberRepository.findById(2L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.requestFollow(principal, 2L));
            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("팔로우 요청 조회")
    class FindMyFollow {

        @Test
        @DisplayName("성공")
        void findMyFollowSuccess() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member follower = createMember(memberId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(1L, following, follower, FollowStatus.REQUESTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(followRepository.findByFollowerAndStatus(any(Member.class), any(FollowStatus.class), any(Pageable.class)))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.findMyFollowRequest(principal, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).status()).isEqualTo(FollowStatus.REQUESTED.name());
        }
    }

    @Nested
    @DisplayName("팔로우 요청 수락")
    class AcceptFollowRequest {

        @Test
        @DisplayName("성공")
        void acceptFollowRequestSuccess() {
            // given
            Long followerId = 1L;
            Long followId = 10L;
            Long notificationId = 100L; // 추가된 notificationId
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when
            FollowResponse response = followService.acceptFollowRequest(principal, followId, notificationId);

            // then
            assertThat(response.status()).isEqualTo(FollowStatus.ACCEPTED.name());
            assertThat(following.getFollowingCount()).isEqualTo(1);
            assertThat(follower.getFollowerCount()).isEqualTo(1);
            verify(notificationService).deleteNotification(notificationId);
            verify(notificationService, times(2)).createNotification(any(Member.class), any(NotificationType.class), nullable(String.class), anyLong());
        }

        @Test
        @DisplayName("실패 - 팔로우 정보를 찾을 수 없음")
        void acceptFollowRequestFailFollowNotFound() {
            // given
            Principal principal = createPrincipal(1L);
            Long notificationId = 100L;
            given(followRepository.findById(10L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.acceptFollowRequest(principal, 10L, notificationId));
            assertEquals(ErrorCode.FOLLOW_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void acceptFollowRequestFailForbidden() {
            // given
            Long wrongFollowerId = 99L;
            Long followerId = 1L;
            Long followId = 10L;
            Long notificationId = 100L;
            Principal principal = createPrincipal(wrongFollowerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.acceptFollowRequest(principal, followId, notificationId));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 이미 팔로우 중")
        void acceptFollowRequestFailAlreadyFollowing() {
            // given
            Long followerId = 1L;
            Long followId = 10L;
            Long notificationId = 100L;
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.ACCEPTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.acceptFollowRequest(principal, followId, notificationId));
            assertEquals(ErrorCode.ALREADY_FOLLOWING, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("팔로워 조회")
    class GetFollower {
        @Test
        @DisplayName("성공 - 공개 계정")
        void getFollowerSuccessPublic() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(2L);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, false);
            Member follower = createMember(3L, false);
            Follow follow = createFollow(1L, follower, member, FollowStatus.ACCEPTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(followRepository.findByFollowerAndStatus(member, FollowStatus.ACCEPTED, pageable))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.getFollower(principal, memberId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 비공개 계정 (본인)")
        void getFollowerSuccessPrivateMine() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, true);
            Member follower = createMember(3L, false);
            Follow follow = createFollow(1L, follower, member, FollowStatus.ACCEPTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(followRepository.findByFollowerAndStatus(member, FollowStatus.ACCEPTED, pageable))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.getFollower(principal, memberId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("실패 - 비공개 계정 (타인, 팔로우 관계 아님)")
        void getFollowerFailPrivateNotMineAndNotFollowing() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(2L);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, true);
            Member principalMember = createMember(2L, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.findById(2L)).willReturn(Optional.of(principalMember));
            given(followRepository.findStatusByFollowerAndFollowing(any(Member.class), any(Member.class))).willReturn(FollowStatus.NONE);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.getFollower(principal, memberId, pageable));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 - 비공개 계정 (타인, 팔로우 관계)")
        void getFollowerSuccessPrivateNotMineAndFollowing() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(2L);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, true);
            Member principalMember = createMember(2L, false);
            Member follower = createMember(3L, false);
            Follow follow = createFollow(1L, follower, member, FollowStatus.ACCEPTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.findById(2L)).willReturn(Optional.of(principalMember));
            given(followRepository.findStatusByFollowerAndFollowing(any(Member.class), any(Member.class))).willReturn(FollowStatus.ACCEPTED);
            given(followRepository.findByFollowerAndStatus(member, FollowStatus.ACCEPTED, pageable))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.getFollower(principal, memberId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("팔로잉 조회")
    class GetFollowing {
        @Test
        @DisplayName("성공 - 공개 계정")
        void getFollowingSuccessPublic() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(2L);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, false);
            Member following = createMember(3L, false);
            Follow follow = createFollow(1L, member, following, FollowStatus.ACCEPTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(followRepository.findByFollowingAndStatus(member, FollowStatus.ACCEPTED, pageable))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.getFollowing(principal, memberId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 비공개 계정 (본인)")
        void getFollowingSuccessPrivateMine() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, true);
            Member following = createMember(3L, false);
            Follow follow = createFollow(1L, member, following, FollowStatus.ACCEPTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(followRepository.findByFollowingAndStatus(member, FollowStatus.ACCEPTED, pageable))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.getFollowing(principal, memberId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("실패 - 비공개 계정 (타인, 팔로우 관계 아님)")
        void getFollowingFailPrivateNotMineAndNotFollowing() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(2L);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, true);
            Member principalMember = createMember(2L, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.findById(2L)).willReturn(Optional.of(principalMember));
            given(followRepository.findStatusByFollowerAndFollowing(any(Member.class), any(Member.class))).willReturn(FollowStatus.NONE);

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.getFollowing(principal, memberId, pageable));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }

        @Test
        @DisplayName("성공 - 비공개 계정 (타인, 팔로우 관계)")
        void getFollowingSuccessPrivateNotMineAndFollowing() {
            // given
            Long memberId = 1L;
            Principal principal = createPrincipal(2L);
            Pageable pageable = PageRequest.of(0, 10);

            Member member = createMember(memberId, true);
            Member principalMember = createMember(2L, false);
            Member following = createMember(3L, false);
            Follow follow = createFollow(1L, member, following, FollowStatus.ACCEPTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.findById(2L)).willReturn(Optional.of(principalMember));
            given(followRepository.findStatusByFollowerAndFollowing(any(Member.class), any(Member.class))).willReturn(FollowStatus.ACCEPTED);
            given(followRepository.findByFollowingAndStatus(member, FollowStatus.ACCEPTED, pageable))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.getFollowing(principal, memberId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("팔로우 관계 확인")
    class IsFollow {

        @Test
        @DisplayName("성공 - 팔로우 관계 없음")
        void isFollowSuccessNone() {
            // given
            Principal principal = createPrincipal(1L);
            Member following = createMember(1L, false);
            Member follower = createMember(2L, false);

            given(memberRepository.findById(1L)).willReturn(Optional.of(following));
            given(memberRepository.findById(2L)).willReturn(Optional.of(follower));
            given(followRepository.findStatusByFollowerAndFollowing(follower, following)).willReturn(null);

            // when
            String status = followService.isFollow(principal, 2L);

            // then
            assertThat(status).isEqualTo("NONE");
        }

        @Test
        @DisplayName("성공 - 팔로우 요청 중")
        void isFollowSuccessRequested() {
            // given
            Principal principal = createPrincipal(1L);
            Member following = createMember(1L, false);
            Member follower = createMember(2L, false);

            given(memberRepository.findById(1L)).willReturn(Optional.of(following));
            given(memberRepository.findById(2L)).willReturn(Optional.of(follower));
            given(followRepository.findStatusByFollowerAndFollowing(follower, following)).willReturn(FollowStatus.REQUESTED);

            // when
            String status = followService.isFollow(principal, 2L);

            // then
            assertThat(status).isEqualTo("REQUESTED");
        }

        @Test
        @DisplayName("성공 - 팔로우 수락됨")
        void isFollowSuccessAccepted() {
            // given
            Principal principal = createPrincipal(1L);
            Member following = createMember(1L, false);
            Member follower = createMember(2L, false);

            given(memberRepository.findById(1L)).willReturn(Optional.of(following));
            given(memberRepository.findById(2L)).willReturn(Optional.of(follower));
            given(followRepository.findStatusByFollowerAndFollowing(follower, following)).willReturn(FollowStatus.ACCEPTED);

            // when
            String status = followService.isFollow(principal, 2L);

            // then
            assertThat(status).isEqualTo("ACCEPTED");
        }
    }

    @Nested
    @DisplayName("팔로우 요청 거절")
    class RejectFollowRequest {

        @Test
        @DisplayName("성공")
        void rejectFollowRequestSuccess() {
            // given
            Long followerId = 1L;
            Long followId = 10L;
            Long notificationId = 100L; // 추가된 notificationId
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when
            String result = followService.rejectFollowRequest(principal, followId, notificationId);

            // then
            assertThat(result).isEqualTo("NONE");
            verify(notificationService).deleteNotification(notificationId);
            verify(followRepository).delete(follow);
        }

        @Test
        @DisplayName("실패 - 팔로우 정보를 찾을 수 없음")
        void rejectFollowRequestFailFollowNotFound() {
            // given
            Principal principal = createPrincipal(1L);
            Long notificationId = 100L;
            given(followRepository.findById(10L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.rejectFollowRequest(principal, 10L, notificationId));
            assertEquals(ErrorCode.FOLLOW_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void rejectFollowRequestFailForbidden() {
            // given
            Long wrongFollowerId = 99L;
            Long followerId = 1L;
            Long followId = 10L;
            Long notificationId = 100L;
            Principal principal = createPrincipal(wrongFollowerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.rejectFollowRequest(principal, followId, notificationId));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 팔로우 상태")
        void rejectFollowRequestFailInvalidStatus() {
            // given
            Long followerId = 1L;
            Long followId = 10L;
            Long notificationId = 100L;
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.ACCEPTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.rejectFollowRequest(principal, followId, notificationId));
            assertEquals(ErrorCode.INVALID_FOLLOW_STATUS, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("팔로우 요청 취소")
    class CancelFollowRequest {

        @Test
        @DisplayName("성공")
        void cancelFollowRequestSuccess() {
            // given
            Long followingId = 1L;
            Long followId = 10L;
            Principal principal = createPrincipal(followingId);

            Member following = createMember(followingId, false);
            Member follower = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when
            String result = followService.cancelFollowRequest(principal, followId);

            // then
            assertThat(result).isEqualTo("NONE");
            verify(followRepository).delete(follow);
        }

        @Test
        @DisplayName("실패 - 팔로우 정보를 찾을 수 없음")
        void cancelFollowRequestFailFollowNotFound() {
            // given
            Long followingId = 1L;
            Long followId = 10L;
            Principal principal = createPrincipal(followingId);

            given(followRepository.findById(followId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.cancelFollowRequest(principal, followId));
            assertEquals(ErrorCode.FOLLOW_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void cancelFollowRequestFailForbidden() {
            // given
            Long wrongFollowingId = 99L;
            Long followId = 10L;
            Principal principal = createPrincipal(wrongFollowingId);

            Member following = createMember(1L, false);
            Member follower = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.cancelFollowRequest(principal, followId));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 팔로우 상태")
        void cancelFollowRequestFailInvalidStatus() {
            // given
            Long followingId = 1L;
            Long followId = 10L;
            Principal principal = createPrincipal(followingId);

            Member following = createMember(followingId, false);
            Member follower = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.ACCEPTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.cancelFollowRequest(principal, followId));
            assertEquals(ErrorCode.INVALID_FOLLOW_STATUS, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("언팔로우")
    class UnFollow {

        @Test
        @DisplayName("성공")
        void unFollowSuccess() {
            // given
            Long followingId = 1L;
            Long followerId = 2L;
            Principal principal = createPrincipal(followingId);

            Member following = createMember(followingId, false);
            Member follower = createMember(followerId, false);
            Follow follow = createFollow(1L, following, follower, FollowStatus.ACCEPTED);

            given(followRepository.findFollowByFollowerAndFollowing(any(Member.class), any(Member.class)))
                    .willReturn(Optional.of(follow));

            // when
            String result = followService.unFollow(principal, followerId);

            // then
            assertThat(result).isEqualTo("NONE");
            verify(followRepository).delete(follow);
            assertThat(following.getFollowingCount()).isZero();
            assertThat(follower.getFollowerCount()).isZero();
        }

        @Test
        @DisplayName("실패 - 팔로우 관계를 찾을 수 없음")
        void unFollowFailFollowNotFound() {
            // given
            Long followingId = 1L;
            Long followerId = 2L;
            Principal principal = createPrincipal(followingId);

            given(followRepository.findFollowByFollowerAndFollowing(any(Member.class), any(Member.class)))
                    .willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.unFollow(principal, followerId));
            assertEquals(ErrorCode.FOLLOW_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("팔로우 요청 조회 (followingId 기준)")
    class FindMyFollowRequestWithFollowingId {

        @Test
        @DisplayName("성공")
        void findMyFollowRequestWithFollowingIdSuccess() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(followingId, false);
            Follow follow = createFollow(1L, following, follower, FollowStatus.REQUESTED);

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.of(following));
            given(followRepository.findFollowByFollowerAndFollowing(follower, following)).willReturn(Optional.of(follow));

            // when
            FollowResponse response = followService.findMyFollowRequestWithFollowingId(principal, followingId);

            // then
            assertThat(response.status()).isEqualTo(FollowStatus.REQUESTED.name());
            assertThat(response.following().memberId()).isEqualTo(followingId);
            assertThat(response.follower().memberId()).isEqualTo(followerId);
        }

        @Test
        @DisplayName("실패 - 팔로워를 찾을 수 없음")
        void findMyFollowRequestWithFollowingIdFailFollowerNotFound() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            Principal principal = createPrincipal(followerId);

            given(memberRepository.findById(followerId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.findMyFollowRequestWithFollowingId(principal, followingId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
            assertThat(exception.getMessage()).contains("Follower member not found");
        }

        @Test
        @DisplayName("실패 - 팔로잉을 찾을 수 없음")
        void findMyFollowRequestWithFollowingIdFailFollowingNotFound() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.findMyFollowRequestWithFollowingId(principal, followingId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
            assertThat(exception.getMessage()).contains("Following member not found");
        }

        @Test
        @DisplayName("실패 - 팔로우 관계를 찾을 수 없음")
        void findMyFollowRequestWithFollowingIdFailFollowNotFound() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;
            Principal principal = createPrincipal(followerId);

            Member follower = createMember(followerId, false);
            Member following = createMember(followingId, false);

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.of(following));
            given(followRepository.findFollowByFollowerAndFollowing(follower, following)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.findMyFollowRequestWithFollowingId(principal, followingId));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FOLLOW_NOT_FOUND);
            assertThat(exception.getMessage()).contains("Follow relationship not found");
        }
    }
}
