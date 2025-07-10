package com.mysite.sns1_server.domain.member.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.domain.member.dto.MemberInfoResponse;
import com.mysite.sns1_server.domain.member.dto.MemberResponse;
import com.mysite.sns1_server.domain.member.service.MemberService;
import com.mysite.sns1_server.global.config.ServerConfig;
import com.mysite.sns1_server.global.security.jwt.service.JwtService;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ServerConfig serverConfig;

    @MockitoBean
    private UserDetailsService userDetailsService;

    

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("signUp: 회원가입 성공")
    @Test
    void t1() throws Exception {
        // given
        JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
        doNothing().when(memberService).join(any(JoinRequest.class));

        // when, then
        mockMvc.perform(post("/api/v1/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isCreated());
    }

    @DisplayName("signUp: 유효성 검증 실패")
    @Test
    void t2() throws Exception {
        // given
        JoinRequest joinRequest = new JoinRequest("", "", "", "");

        // when, then
        mockMvc.perform(post("/api/v1/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("getMemberInfo: 회원 정보 조회 성공")
    @Test
    void getMemberInfo_success() throws Exception {
        // given
        Long memberId = 1L;
        MemberInfoResponse memberInfoResponse = new MemberInfoResponse(memberId, "testUser", "profile.jpg", "intro", 10L, 20L);
        when(memberService.getMemberInfo(memberId)).thenReturn(memberInfoResponse);

        // when, then
        mockMvc.perform(get("/api/v1/members/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(memberId))
                .andExpect(jsonPath("$.data.username").value("testUser"));
    }

    @DisplayName("searchMemberByName: 회원 이름으로 검색 성공")
    @Test
    void searchMemberByName_success() throws Exception {
        // given
        String username = "testUser";
        MemberResponse memberResponse = new MemberResponse(1L, "testUser", "profile.jpg");
        when(memberService.searchMemberByUsername(username)).thenReturn(memberResponse);

        // when, then
        mockMvc.perform(get("/api/v1/members").param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("testUser"));
    }
}
