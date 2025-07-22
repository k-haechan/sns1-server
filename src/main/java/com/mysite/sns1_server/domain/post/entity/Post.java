package com.mysite.sns1_server.domain.post.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.dto.PostRequest;
import com.mysite.sns1_server.global.aws.s3.service.S3Service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
	@Id
	@Setter(AccessLevel.PROTECTED)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(updatable = false, nullable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private Instant updatedAt;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(nullable = false, length = 2000)
	private String content;

	@Builder.Default
	@Column(nullable = false, length = 100)
	private Long likeCount = 0L;

	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private Member author;

	public Post(Member author, String title, String content) {
		this.author = author;
		this.title = title;
		this.content = content;
	}

	public void updateBy(PostRequest request) {
		this.title = request.title();
		this.content = request.content();
	}

	public String getThumbnailUrl(S3Service s3Service) {
		return s3Service.getPostImagePath(author.getId(),getId(),0);
	}
}
