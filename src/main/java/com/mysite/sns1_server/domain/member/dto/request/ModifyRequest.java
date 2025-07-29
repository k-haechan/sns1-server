package com.mysite.sns1_server.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ModifyRequest(
	@JsonProperty("real_name")
	String realName,
	@JsonProperty("profile_image_url")
	String profileImageUrl,
	String introduction,
	@JsonProperty("is_secret")
	Boolean isSecret
) {
}
