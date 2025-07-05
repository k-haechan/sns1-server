package com.mysite.sns1_server.domain.member.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final RedisService redisService;

	public void join(JoinRequest request) {
		String email = request.email();
		if (!redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, email)) {
			throw new CustomException(ErrorCode.EMAIL_VERIFY_EXPIRED);
		}

		try {
			Member newMember = request.toEntity();
			memberRepository.save(newMember);
		} catch (DataIntegrityViolationException e) {
			String message = e.getMessage();

			if (message.contains("uc_member_email")) {
				throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
			}
			if (message.contains("uc_member_username")) {
				throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
			}
			throw new CustomException(ErrorCode.DATABASE_ERROR);
		}

	}
}
