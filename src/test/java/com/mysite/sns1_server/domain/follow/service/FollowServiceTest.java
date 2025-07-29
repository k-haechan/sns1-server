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

    private Member createMember(Long id, boolean isSecret) {
        Member member = Member.builder().isSecret(isSecret).build();
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "followerCount", 0L);
        ReflectionTestUtils.setField(member, "followingCount", 0L);
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
            FollowResponse response = followService.requestFollow(1L, 2L);

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
            FollowResponse response = followService.requestFollow(1L, 2L);

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
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.requestFollow(1L, 2L));
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
            Principal principal = () -> String.valueOf(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member follower = createMember(memberId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(1L, following, follower, FollowStatus.REQUESTED);
            Slice<Follow> followSlice = new SliceImpl<>(List.of(follow), pageable, false);

            given(followRepository.findByFollowerAndStatus(any(Member.class), any(FollowStatus.class), any(Pageable.class)))
                    .willReturn(followSlice);

            // when
            Slice<FollowResponse> response = followService.findMyFollow(principal, pageable);

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

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when
            FollowResponse response = followService.acceptFollowRequest(followerId, followId);

            // then
            assertThat(response.status()).isEqualTo(FollowStatus.ACCEPTED.name());
            assertThat(following.getFollowingCount()).isEqualTo(1);
            assertThat(follower.getFollowerCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 팔로우 정보를 찾을 수 없음")
        void acceptFollowRequestFailFollowNotFound() {
            // given
            given(followRepository.findById(10L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.acceptFollowRequest(1L, 10L));
            assertEquals(ErrorCode.FOLLOW_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void acceptFollowRequestFailForbidden() {
            // given
            Long wrongFollowerId = 99L;
            Long followerId = 1L;
            Long followId = 10L;

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.REQUESTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.acceptFollowRequest(wrongFollowerId, followId));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 잘못된 팔로우 상태")
        void acceptFollowRequestFailInvalidStatus() {
            // given
            Long followerId = 1L;
            Long followId = 10L;

            Member follower = createMember(followerId, false);
            Member following = createMember(2L, false);
            Follow follow = createFollow(followId, following, follower, FollowStatus.ACCEPTED);

            given(followRepository.findById(followId)).willReturn(Optional.of(follow));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> followService.acceptFollowRequest(followerId, followId));
            assertEquals(ErrorCode.INVALID_FOLLOW_STATUS, exception.getErrorCode());
        }
    }
}
