package com.mysite.sns1_server.domain.auth.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sns1_server.domain.auth.dto.request.CodeRequest;
import com.mysite.sns1_server.domain.auth.dto.request.LoginRequest;
import com.mysite.sns1_server.domain.auth.dto.request.VerifyRequest;
import com.mysite.sns1_server.domain.auth.service.EmailService;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
import com.mysite.sns1_server.domain.member.service.MemberService;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.security.jwt.service.AccessTokenService;
import com.mysite.sns1_server.global.security.jwt.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;

@DisplayName("AuthController 단위 테스트")
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private RedisService redisService; // RedisService가 필요하다면 추가

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;



    @DisplayName("로그인 성공")
    @Test
    void loginSuccess() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        MemberBriefResponse loginResponse = new MemberBriefResponse(1L, "testuser", "test", "test.com");
        given(memberService.login(any(LoginRequest.class))).willReturn(loginResponse);
        given(accessTokenService.generateToken(anyLong())).willReturn("accessToken");
        given(refreshTokenService.generateToken(anyLong())).willReturn("refreshToken");
        given(accessTokenService.getTokenName()).willReturn("access-token");
        given(refreshTokenService.getTokenName()).willReturn("refresh-token");
        given(accessTokenService.getExpiration()).willReturn(Duration.ofMillis(3600000L));
        given(refreshTokenService.getExpiration()).willReturn(Duration.ofMillis(86400000L));


        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("access-token", "accessToken"))
                .andExpect(cookie().value("refresh-token", "refreshToken"))
                .andExpect(jsonPath("$.message").value("로그인 성공, 토큰이 생성되었습니다."))
                .andExpect(jsonPath("$.data.member_id").value(1L))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.profile_image_url").value("test.com"))
                .andExpect(jsonPath("$.data.real_name").value("test"));
    }

    @DisplayName("이메일 인증코드 전송 성공")
    @Test
    void sendCodeSuccess() throws Exception {
        // given
        CodeRequest codeRequest = new CodeRequest("test@test.com");
        doNothing().when(emailService).sendCode(codeRequest.email());

        // when & then
        mockMvc.perform(post("/api/v1/auth/email/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증코드가 성공적으로 전송되었습니다."));
    }

    @DisplayName("인증코드 유효성 확인 성공")
    @Test
    void verifyCodeSuccess() throws Exception {
        // given
        VerifyRequest verifyRequest = new VerifyRequest("test@test.com", "123456");
        doNothing().when(emailService).verifyCode(verifyRequest.email(), verifyRequest.code());

        // when & then
        mockMvc.perform(post("/api/v1/auth/email/code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증코드가 성공적으로 확인되었습니다. 10분 동안 유효합니다."));
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logoutSuccess() throws Exception {
        // given
        given(accessTokenService.getTokenName()).willReturn("access-token");
        given(refreshTokenService.getTokenName()).willReturn("refresh-token");
        given(refreshTokenService.getLeftExpirationTime(anyString())).willReturn(Duration.ofMillis(1000L));

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                .cookie(
                        new Cookie("access-token", "accessTokenValue"),
                        new Cookie("refresh-token", "refreshTokenValue")
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃이 성공적으로 완료되었습니다."));


        then(redisService).should().set(any(), anyString(), anyString(), any(Duration.class));
    }
}
