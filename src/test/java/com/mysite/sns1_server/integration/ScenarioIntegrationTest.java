package com.mysite.sns1_server.integration;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sns1_server.domain.auth.dto.CodeRequest;
import com.mysite.sns1_server.domain.auth.dto.LoginRequest;
import com.mysite.sns1_server.domain.auth.dto.VerifyRequest;
import com.mysite.sns1_server.domain.auth.service.EmailService;
import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;

import jakarta.persistence.EntityManager;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScenarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RedisService redisService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // RedisService Mocking (상태를 가지도록 설정)
        final Map<String, Object> redisStore = new ConcurrentHashMap<>();

        willAnswer(invocation -> {
            RedisKeyType type = invocation.getArgument(0);
            String param = invocation.getArgument(1);
            Object value = invocation.getArgument(2);
            redisStore.put(type.getKey(param), value);
            return null;
        }).given(redisService).set(any(RedisKeyType.class), anyString(), any());

        willAnswer(invocation -> {
            RedisKeyType type = invocation.getArgument(0);
            String param = invocation.getArgument(1);
            Object value = invocation.getArgument(2);
            Duration duration = invocation.getArgument(3);
            redisStore.put(type.getKey(param), value);
            return null;
        }).given(redisService).set(any(RedisKeyType.class), anyString(), any(), any(Duration.class));


        given(redisService.get(any(RedisKeyType.class), anyString())).willAnswer(invocation -> {
            RedisKeyType type = invocation.getArgument(0);
            String param = invocation.getArgument(1);
            return redisStore.get(type.getKey(param));
        });

        willAnswer(invocation -> {
            RedisKeyType type = invocation.getArgument(0);
            String param = invocation.getArgument(1);
            redisStore.remove(type.getKey(param));
            return null;
        }).given(redisService).delete(any(RedisKeyType.class), anyString());

        given(redisService.hasKey(any(RedisKeyType.class), anyString())).willAnswer(invocation -> {
            RedisKeyType type = invocation.getArgument(0);
            String param = invocation.getArgument(1);
            return redisStore.containsKey(type.getKey(param));
        });
    }

    @DisplayName("이메일 인증 후 회원가입 시나리오")
    @Test
    void emailVerificationAndJoinScenario() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String username = "testuser";
        String realName = "테스트 유저";
        String code = "123456";

        // Mocking: emailService.sendCode는 아무 동작도 하지 않도록 설정
        doNothing().when(emailService).sendCode(anyString());

        // Mocking: emailService.verifyCode는 실제 Redis에 인증 완료 상태를 저장하도록 설정
        doAnswer(invocation -> {
            String emailArg = invocation.getArgument(0);
            // 실제 로직과 유사하게, 인증 코드가 일치하는지 확인 (테스트에서는 항상 참)
            // 그리고 VERIFIED_EMAIL 키를 저장
            redisService.set(RedisKeyType.VERIFIED_EMAIL, emailArg, "VERIFIED");
            return null;
        }).when(emailService).verifyCode(anyString(), anyString());


        // when & then
        // 1. 이메일 인증 코드 전송
        CodeRequest codeRequest = new CodeRequest(email);
        mockMvc.perform(post("/api/v1/auth/email/code").with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증코드가 성공적으로 전송되었습니다."));

        // 2. 이메일 인증 코드 확인
        VerifyRequest verifyRequest = new VerifyRequest(email, code);
        mockMvc.perform(post("/api/v1/auth/email/code/verify").with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증코드가 성공적으로 확인되었습니다. 10분 동안 유효합니다."));

        // 3. 회원가입
        JoinRequest joinRequest = new JoinRequest(username, password, realName, email);
        mockMvc.perform(post("/api/v1/members/join").with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 성공적으로 완료되었습니다."));
    }

    @DisplayName("로그인 후 로그아웃 시나리오")
    @Test
    void loginAndLogoutScenario() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String username = "testuser";
        String realName = "테스트 유저";

        // 이메일 인증이 완료된 상태로 설정
        redisService.set(RedisKeyType.VERIFIED_EMAIL, email, "VERIFIED");

        // 1. 회원가입 (테스트를 위해 미리 사용자를 생성)
        JoinRequest joinRequest = new JoinRequest(username, password, realName, email);
        mockMvc.perform(post("/api/v1/members/join").with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isCreated());

        // when & then
        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest(username, password);
        var resultActions = mockMvc.perform(post("/api/v1/auth/login").with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access-token"))
                .andExpect(cookie().exists("refresh-token"))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.realName").value(realName));

        // 3. 로그아웃
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(user(username)) // 로그인된 사용자로 요청
                        .cookie(resultActions.andReturn().getResponse().getCookies()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃이 성공적으로 완료되었습니다."));
    }
}
