package com.mysite.sns1_server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws") // WebSocket 접속 경로
			.setAllowedOriginPatterns("*") // 개발용: CORS 허용
			.withSockJS(); // SockJS fallback
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/queue"); // 1:1 개인 채널
		registry.setApplicationDestinationPrefixes("/app"); // 메시지 전송 prefix
		registry.setUserDestinationPrefix("/user"); // 대상 유저 prefix
	}
}
