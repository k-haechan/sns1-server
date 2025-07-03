package com.mysite.sns1_server.domain.post.entity;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Post extends BaseEntity {

	@Column(nullable = false, length = 500)
	private String title;

	@Column(nullable = false, length = 2000)
	private String content;

	@Column(nullable = false, length = 100)
	private Long likeCount = 0L;

	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;
}
