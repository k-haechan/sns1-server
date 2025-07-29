package com.mysite.sns1_server.domain.member.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.mysite.sns1_server.domain.member.dto.request.ModifyRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreatedDate
	@Column(updatable = false, nullable = false)
	private Instant createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private Instant updatedAt;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false, length = 100)
	private String password;

	@Column(nullable = false, length = 50)
	private String realName;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(length = 255)
	private String profileImageUrl;

	@Column(length = 255)
	private String introduction;

	@Column(length = 255, nullable = false)
	private Long followerCount;

	@Column(length = 255, nullable = false)
	private Long followingCount;

	@Column
	@Builder.Default
	private Boolean isSecret = false;

	public Member modify(ModifyRequest request) {
		this.realName = request.realName();
		this.introduction = request.introduction();
		this.isSecret = request.isSecret();
		return this;
	}

	public void addFollower() {
		this.followerCount++;
	}
	public void addFollowing() {
		this.followingCount++;
	}

	public void removeFollower() {
		if (this.followerCount > 0) {
			this.followerCount--;
		}
	}

	public void removeFollowing() {
		if (this.followingCount > 0) {
			this.followingCount--;
		}
	}




	public static Member createActor(Long id) {
		return Member.builder().id(id).build();
	}
}
