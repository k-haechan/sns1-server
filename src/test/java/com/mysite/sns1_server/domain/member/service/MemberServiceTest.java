package com.mysite.sns1_server.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyString;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mysite.sns1_server.domain.auth.dto.LoginRequest;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @DisplayName("join: 회원가입 성공")
    @Test
    void t1() {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        when(redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, joinRequest.email())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

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

    @DisplayName("login: 로그인 성공")
    @Test
    void loginSuccess() {
        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(member.getPassword()).thenReturn("encodedPassword");

        when(memberRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(loginRequest.password(), member.getPassword())).thenReturn(true);

        // when
        Long memberId = memberService.login(loginRequest);

        // then
        assertThat(memberId).isEqualTo(member.getId());
    }

    @DisplayName("login: 로그인 실패 - 사용자 없음")
    @Test
    void loginFailure_memberNotFound() {
        // given
        LoginRequest loginRequest = new LoginRequest("nonexistentUser", "password");
        when(memberRepository.findByUsername(loginRequest.username())).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @DisplayName("login: 로그인 실패 - 비밀번호 불일치")
    @Test
    void loginFailure_invalidPassword() {
        // given
        LoginRequest loginRequest = new LoginRequest("testUser", "wrongpassword");
        Member member = mock(Member.class);
        when(member.getPassword()).thenReturn("encodedPassword");

        when(memberRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(loginRequest.password(), member.getPassword())).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> memberService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_CREDENTIAL);
    }
}