package com.mysite.sns1_server.domain.member.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.auth.dto.LoginRequest;
import com.mysite.sns1_server.domain.auth.dto.LoginResponse;
import com.mysite.sns1_server.domain.member.dto.JoinRequest;
import com.mysite.sns1_server.domain.member.dto.response.MemberResponse;
import com.mysite.sns1_server.domain.member.dto.MemberInfoResponse;
import com.mysite.sns1_server.domain.member.dto.MemberResponse;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.global.cache.RedisKeyType;
import com.mysite.sns1_server.global.cache.RedisService;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
	private final MemberRepository memberRepository;
	private final RedisService redisService;
	private final PasswordEncoder passwordEncoder;

	public void join(JoinRequest request) {
		String email = request.email();
		if (!redisService.hasKey(RedisKeyType.VERIFIED_EMAIL, email)) {
			throw new CustomException(ErrorCode.EMAIL_VERIFY_EXPIRED);
		}

		try {
			Member newMember = request.toEntity(passwordEncoder);
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

	public LoginResponse login(LoginRequest request) {
		String username = request.username();
		String password = request.password();

		Member member = memberRepository.findByUsername(username)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(password, member.getPassword())) {
			throw new CustomException(ErrorCode.BAD_CREDENTIAL);
		}
		return LoginResponse.from(member);
	}

	public MemberResponse getMemberById(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		return MemberResponse.from(member);
	}

	public MemberInfoResponse getMemberInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		return MemberInfoResponse.from(member);

	}

	public MemberResponse searchMemberByUsername(String username) {
		Member member = memberRepository.findByUsername(username)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		return MemberResponse.from(member);
	}
}
