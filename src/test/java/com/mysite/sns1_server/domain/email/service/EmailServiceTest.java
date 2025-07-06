package com.mysite.sns1_server.domain.email.service;

import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("EmailService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@Mock
	private RedisService redisService;

	@InjectMocks
	private EmailService emailService;

	@DisplayName("sendCode: 이메일 전송 및 Redis 저장 성공")
	@Test
	void t1() {
		// given
		String email = "test@example.com";
		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

		// when
		emailService.sendCode(email);

		// then
		verify(redisService).set(eq(RedisKeyType.AUTH_EMAIL), eq(email), anyString());
		verify(javaMailSender).send(mimeMessage);
	}

	@DisplayName("verifyCode: 인증 코드 일치 시 Redis 갱신")
	@Test
	void t2() {
		// given
		String email = "test@example.com";
		String code = "123456";
		when(redisService.get(RedisKeyType.AUTH_EMAIL, email)).thenReturn(code);

		// when
		emailService.verifyCode(email, code);

		// then
		verify(redisService).delete(RedisKeyType.AUTH_EMAIL, email);
		verify(redisService).set(RedisKeyType.AUTH_EMAIL, email, code);
	}

	@DisplayName("verifyCode: 인증 코드 불일치 시 예외 발생")
	@Test
	void t3() {
		// given
		String email = "test@example.com";
		String wrongCode = "000000";
		String actualCode = "123456";
		when(redisService.get(RedisKeyType.AUTH_EMAIL, email)).thenReturn(actualCode);

		// when, then
		assertThatThrownBy(() -> emailService.verifyCode(email, wrongCode))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFY_FAILED);
	}

	@DisplayName("verifyCode: Redis에 값이 없는 경우 예외 발생")
	@Test
	void t4() {
		// given
		String email = "test@example.com";
		String code = "123456";
		when(redisService.get(RedisKeyType.AUTH_EMAIL, email)).thenReturn(null);

		// when, then
		assertThatThrownBy(() -> emailService.verifyCode(email, code))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFY_FAILED);
	}

	@DisplayName("sendCode: 이메일 전송 실패 시 예외 발생")
	@Test
	void t5() {
		// given
		String email = "fail@example.com";
		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
		doThrow(new RuntimeException()).when(javaMailSender).send((MimeMessage)any());

		// when, then
		assertThatThrownBy(() -> emailService.sendCode(email))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_SEND_FAILED);
	}
}
