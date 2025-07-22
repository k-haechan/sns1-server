package com.mysite.sns1_server.domain.image.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public record ImageRequest(
	@NotNull(message = "업로드하실 사진의 개수를 알려주세요.")
	@JsonProperty("images-length")
	Integer imagesLength
) {
}


