package com.mysite.sns1_server.domain.comment.dto.request;

import com.mysite.sns1_server.domain.comment.entity.Comment;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.entity.Post;

public record CommentRequest(
	String content
) {
	public Comment toComment(Post post, Member author) {
		return Comment.create(content, post, author);
	}
}
