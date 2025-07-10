package com.mysite.sns1_server.domain.member.dto;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.mysite.sns1_server.domain.member.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
	@NotBlank
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	@Schema(description = "회원가입할 사용자의 username", example = "testUser")
	String username,

	@NotBlank
	@Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
	@Schema(description = "회원가입할 사용자의 password", example = "password123")
	String password,

	@NotBlank
	@Size(max = 50)
	@Schema(description = "회원가입할 사용자의 realName", example = "테스트 유저")
	String realName,

	@NotBlank
	@Email
	@Size(max = 100)
	@Schema(description = "회원가입할 사용자의 email", example = "testUser@email.com")
	String email
) {
	public Member toEntity(PasswordEncoder passwordEncoder) {
		return Member.builder()
			.username(username)
			.password(passwordEncoder.encode(password))
			.realName(realName)
			.email(email)
			.followerCount(0L)
			.followingCount(0L)
			.build();
	}
}
