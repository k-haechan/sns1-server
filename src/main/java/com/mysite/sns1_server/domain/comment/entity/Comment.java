package com.mysite.sns1_server.domain.comment.entity;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Getter
@Entity
public class Comment extends BaseEntity {

	@Column(nullable = false, length = 255)
	private String content;

	@Column(nullable = false)
	private Long likeCount = 0L;

	@Column(nullable = false)
	private Long reCommentCount = 0L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	public static Comment create(String content, Post post, Member author) {
		Comment comment = new Comment();
		comment.content = content;
		comment.post = post;
		comment.author = author;
		return comment;
	}

	public void updateContent(String content) {
		this.content = content;
	}
}
