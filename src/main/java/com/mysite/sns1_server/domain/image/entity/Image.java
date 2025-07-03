package com.mysite.sns1_server.domain.image.entity;

import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Image extends BaseEntity {

	@Column(length = 255, nullable = false)
	private String url;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private Post post;
}
