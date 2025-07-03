package com.mysite.sns1_server.domain.post.entity;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PostLike extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "liker_id", nullable = false)
	private Member liker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
}
