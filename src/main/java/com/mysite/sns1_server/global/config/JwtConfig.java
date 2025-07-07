package com.mysite.sns1_server.global.config;

import java.time.Duration;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mysite.sns1_server.global.security.jwt.service.AccessTokenService;
import com.mysite.sns1_server.global.security.jwt.service.RefreshTokenService;

import io.jsonwebtoken.security.Keys;

/**
 * JWT 관련 설정 정보를 관리하는 Config 클래스
 * - application.yml 또는 properties 에서 secret, 만료시간을 주입받아 SecretKey 생성 및 보관
 * - JwtType(ACCESS_TOKEN, REFRESH_TOKEN)별로 JwtProperties 제공
 */
@Configuration
public class JwtConfig {

	@Value("${custom.jwt.access-token.secret}")
	private String accessSecret; // AccessToken 서명용 시크릿 문자열

	@Value("${custom.jwt.refresh-token.secret}")
	private String refreshSecret; // RefreshToken 서명용 시크릿 문자열

	@Value("${custom.jwt.access-token.expiration-time}")
	private Duration accessExpiration; // AccessToken 만료시간

	@Value("${custom.jwt.refresh-token.expiration-time}")
	private Duration refreshExpiration; // RefreshToken 만료시간


	@Bean
	public AccessTokenService accessTokenService() {
		SecretKey secretKey = Keys.hmacShaKeyFor(accessSecret.getBytes());
		return new AccessTokenService("access-token", accessExpiration, secretKey);
	}

	@Bean
	public RefreshTokenService refreshTokenService() {
		SecretKey secretKey = Keys.hmacShaKeyFor(refreshSecret.getBytes());
		return new RefreshTokenService("refresh-token", refreshExpiration, secretKey);
	}
}
