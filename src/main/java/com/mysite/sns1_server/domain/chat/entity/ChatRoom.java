package com.mysite.sns1_server.domain.chat.entity;

import com.mysite.sns1_server.global.baseEntity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

	@Column(length = 255)
	private Long lowerMemberId;

	@Column(length = 255)
	private Long upperMemberId;
}
