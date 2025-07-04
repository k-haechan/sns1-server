package com.mysite.sns1_server.domain.email.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sns1_server.domain.email.dto.request.CodeRequest;
import com.mysite.sns1_server.domain.email.dto.request.VerifyRequest;
import com.mysite.sns1_server.domain.email.service.EmailService;

@DisplayName("EmailController 단위 테스트")
@WebMvcTest(EmailController.class)
class EmailControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmailService emailService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("이메일 인증코드 전송 - 성공")
	void t1() throws Exception {
		// given
		CodeRequest request = new CodeRequest("test@example.com");

		// when
		ResultActions actions = mockMvc.perform(post("/api/v1/email/code")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// then
		actions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("이메일 인증코드가 성공적으로 전송되었습니다."));

		verify(emailService).sendCode("test@example.com");
	}

	@Test
	@DisplayName("인증코드 검증 - 성공")
	void t2() throws Exception {
		// given
		VerifyRequest request = new VerifyRequest("test@example.com", "123456");

		// when
		ResultActions actions = mockMvc.perform(post("/api/v1/email/code/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// then
		actions
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("인증코드가 성공적으로 확인되었습니다. 10분 동안 유효합니다."));

		verify(emailService).verifyCode("test@example.com", "123456");
	}
}
