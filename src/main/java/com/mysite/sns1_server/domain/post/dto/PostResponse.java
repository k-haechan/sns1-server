package com.mysite.sns1_server.domain.post.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
import com.mysite.sns1_server.domain.post.entity.Post;

public record PostResponse(
	@JsonProperty("post_id")
	Long postId,
	String title,
	String content,
	List<ImageResponse> images
) {
	public static PostResponse from(Post newPost, List<ImageResponse> imageResponses) {
		return new PostResponse(
			newPost.getId(),
			newPost.getTitle(),
			newPost.getContent(),
			imageResponses
		);
	}
}
