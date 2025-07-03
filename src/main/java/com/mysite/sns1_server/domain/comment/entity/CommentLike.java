package com.mysite.sns1_server.domain.comment.entity;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CommentLike extends BaseEntity {

	@ManyToOne(fetch =  FetchType.LAZY)
	@JoinColumn(name = "liker_id", nullable = false)
	private Member liker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;
}
