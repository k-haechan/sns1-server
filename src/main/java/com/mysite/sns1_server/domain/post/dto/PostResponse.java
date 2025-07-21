package com.mysite.sns1_server.domain.post.dto;

import java.util.List;

import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
import com.mysite.sns1_server.domain.post.entity.Post;

public record PostResponse(
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
