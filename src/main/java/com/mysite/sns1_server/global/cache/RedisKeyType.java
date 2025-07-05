package com.mysite.sns1_server.global.cache;

import java.time.Duration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RedisKeyType {
	AUTH_EMAIL("auth:email:%s", Duration.ofMinutes(3)), // 인증용 전화번호 키, TTL 5분
	BLACKLIST("blacklist:%s", Duration.ofDays(7)); // 블랙리스트 키, TTL 30일

	private final String keyPattern;
	private final Duration ttl;

	public String getKey(String param) {
		return String.format(keyPattern, param);
	}

	public Duration getTtl() {
		return ttl;
	}


}
