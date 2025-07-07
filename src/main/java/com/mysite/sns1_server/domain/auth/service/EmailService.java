package com.mysite.sns1_server.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
	private final JavaMailSender javaMailSender;

	private final RedisService redisService;
	private final SpringTemplateEngine springTemplateEngine;

	private String htmlTemplateToString(String htmlUrl ,String title, String code) {
		Context context = new Context();
		context.setVariable("title", title);
		context.setVariable("code", code);

		return springTemplateEngine.process(htmlUrl, context);
	}


	private void sendMimeMessage(String email, String subject, String html) {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

			// 메일을 받을 수신자 설정
			mimeMessageHelper.setTo(email);
			// 메일의 제목 설정
			mimeMessageHelper.setSubject(subject);

			// html 문법 적용한 메일의 내용
			String content = String.format(html);

			// 메일의 내용 설정
			mimeMessageHelper.setText(content, true);

			javaMailSender.send(mimeMessage);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

	@Async
	public void sendCode(String email) {
		// 랜덤 인증코드 생성
		SecureRandom random = new SecureRandom();

		// 인증 코드 email 전송
		String subject = "[sns.haechan.site] 인증번호 발송";
		String title = "이메일 인증번호";
		String code = String.format("%06d", random.nextInt(1_000_000));

		String html = htmlTemplateToString("email/verify", title, code);

		sendMimeMessage(email, subject, html);

		// Redis에 인증 코드 저장
		redisService.set(RedisKeyType.AUTH_EMAIL, email, code);
	}

	public void verifyCode(String email, String code) {
		String cachedCode = (String) redisService.get(RedisKeyType.AUTH_EMAIL, email);

		if (cachedCode == null || !cachedCode.equals(code)) {
			throw new CustomException(ErrorCode.EMAIL_VERIFY_FAILED);
		}
		redisService.delete(RedisKeyType.AUTH_EMAIL, email);

		redisService.set(RedisKeyType.VERIFIED_EMAIL, email, code);
	}
}
