package com.mysite.sns1_server.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.entity.Post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public record PostRequest(
	String title,
	String content,
	@Max(value = 5, message = "사진은 최대 5장까지 게시할 수 있습니다.")
	@NotNull(message = "images-length는 필수로 입력해주어야 합니다.")
	@JsonProperty("images-length")
	Integer imagesLength
) {
	public Post toPost(Member member) {
		return Post.builder()
			.author(member)
			.title(title)
			.content(content)
			.likeCount(0L)
			.build();
	}
}
