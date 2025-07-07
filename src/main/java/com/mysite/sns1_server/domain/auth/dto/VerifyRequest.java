package com.mysite.sns1_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
	@NotBlank(message = "이메일은 필수입니다.")
	@Schema(description = "이메일 인증을 위한 이메일 주소", example = "testUser@email.com")
	String email,

	@NotBlank(message = "인증코드는 필수입니다.")
	@Schema(description = "이메일 인증 확인을 위한 인증 코드", example = "123456")
	String code
) {
}
