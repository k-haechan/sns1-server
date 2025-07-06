package com.mysite.sns1_server.domain.member.dto;

import com.mysite.sns1_server.domain.member.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
	@NotBlank
	@Size(max = 50)
	@Schema(description = "회원가입할 사용자의 username", example = "testUser")
	String username,

	@NotBlank
	@Size(max = 100)
	@Schema(description = "회원가입할 사용자의 realName", example = "테스트")
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
	public Member toEntity() {
		return Member.builder()
			.username(username)
			.password(password)
			.realName(realName)
			.email(email)
			.build();
	}
}
