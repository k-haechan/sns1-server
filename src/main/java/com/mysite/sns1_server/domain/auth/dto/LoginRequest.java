package com.mysite.sns1_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
	@NotBlank
	@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
	@Schema(description = "로그인할 사용자의 username", example = "testUser")
	String username,
	@NotBlank
	@Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
	@Schema(description = "로그인할 사용자의 password", example = "password123")
	String password
) {}
