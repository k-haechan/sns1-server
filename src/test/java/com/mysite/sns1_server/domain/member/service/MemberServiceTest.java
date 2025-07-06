package com.mysite.sns1_server.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

@DisplayName("MemberService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("join: 회원가입 성공")
    @Test
    void t1() {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        when(redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, joinRequest.email())).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenReturn(joinRequest.toEntity());

        // when
        memberService.join(joinRequest);

        // then
        verify(memberRepository).save(any(Member.class));
    }

    @DisplayName("join: 이메일 미인증 시 예외 발생")
    @Test
    void t2() {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        when(redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, joinRequest.email())).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> memberService.join(joinRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFY_EXPIRED);
    }

    @DisplayName("join: 이메일 중복 시 예외 발생")
    @Test
    void t3() {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        when(redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, joinRequest.email())).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenThrow(new DataIntegrityViolationException("uc_member_email"));

        // when, then
        assertThatThrownBy(() -> memberService.join(joinRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATE);
    }

    @DisplayName("join: 사용자 이름 중복 시 예외 발생")
    @Test
    void t4() {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        when(redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, joinRequest.email())).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenThrow(new DataIntegrityViolationException("uc_member_username"));

        // when, then
        assertThatThrownBy(() -> memberService.join(joinRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATE);
    }

    @DisplayName("join: 데이터베이스 오류 발생 시 예외 발생")
    @Test
    void t5() {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        when(redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, joinRequest.email())).thenReturn(true);
        when(memberRepository.save(any(Member.class))).thenThrow(new DataIntegrityViolationException("other constraint"));

        // when, then
        assertThatThrownBy(() -> memberService.join(joinRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATABASE_ERROR);
    }
}
