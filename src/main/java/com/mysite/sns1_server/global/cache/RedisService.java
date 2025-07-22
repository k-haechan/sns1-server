package com.mysite.sns1_server.global.cache;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
	private final RedisTemplate<String, Object> redisTemplate;

	public void set(RedisKeyType type, String param, Object value) {
		redisTemplate.opsForValue().set(type.getKey(param), value, type.getTtl());
	}

	public void set(RedisKeyType type, String param, Object value, Duration timeout) {
		redisTemplate.opsForValue().set(type.getKey(param), value, timeout);
	}

	public Object get(RedisKeyType type, String param) {
		return redisTemplate.opsForValue().get(type.getKey(param));
	}

	public void expire(RedisKeyType type, String param, Duration timeout) {
		redisTemplate.expire(type.getKey(param), timeout);
	}

	public boolean hasKey(RedisKeyType type, String param) {
		return redisTemplate.hasKey(type.getKey(param));
	}

	public void delete(RedisKeyType type, String param) {
		redisTemplate.delete(type.getKey(param));
	}
}
