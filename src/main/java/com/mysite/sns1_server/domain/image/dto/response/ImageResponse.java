package com.mysite.sns1_server.domain.image.dto.response;

import com.mysite.sns1_server.domain.image.entity.Image;

public record ImageResponse(String url) {
	public static ImageResponse from(Image image, String cdnHost) {
		return new ImageResponse(cdnHost + image.getPath());
	}

	public static ImageResponse from(String url) {
		return new ImageResponse(url);
	}
}
