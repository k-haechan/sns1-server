package com.mysite.sns1_server.domain.member.entity;

import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {

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

}
