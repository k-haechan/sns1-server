package com.mysite.sns1_server.config;

import java.util.concurrent.Executor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AsyncSyncTestConfig {
	@Bean
	public Executor taskExecutor() {
		return Runnable::run; // 비동기처럼 보이지만 실제로는 동기 실행
	}
}
