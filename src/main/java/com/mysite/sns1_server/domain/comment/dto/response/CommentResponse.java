package com.mysite.sns1_server.domain.comment.dto.response;

import java.time.Instant;

import com.mysite.sns1_server.domain.comment.entity.Comment;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;

public record CommentResponse(
	String content,
	Long postId,
	Long id,
	Long likeCount,
	Long reCommentCount,
	Instant createdAt,
	MemberBriefResponse member
) {
	public static CommentResponse from(Comment savedComment) {
		return new CommentResponse(
			savedComment.getContent(),
			savedComment.getPost().getId(),
			savedComment.getId(),
			savedComment.getLikeCount(),
			savedComment.getReCommentCount(),
			savedComment.getCreatedAt(),
			MemberBriefResponse.from(savedComment.getAuthor())
		);
	}
}
