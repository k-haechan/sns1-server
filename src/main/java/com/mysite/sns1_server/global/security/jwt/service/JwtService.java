package com.mysite.sns1_server.global.security.jwt.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class JwtService {
	@Getter
	private final String tokenName;
	@Getter
	private final Duration expiration;
	private final SecretKey secretKey;



	public String generateToken(Long memberId) {
		Date now = new Date();
		Date expiry = Date.from(now.toInstant().plus(expiration));

		JwtBuilder jwtBuilder = Jwts.builder()
			.subject(memberId.toString())
			.issuedAt(now)
			.expiration(expiry)
			.signWith(secretKey);

	return jwtBuilder.compact();
	}

	public Claims parseClaims(String token) {
		try {
			// JWT 파서 빌드 및 토큰 파싱
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

		} catch (ExpiredJwtException e) {
			// 토큰 만료 시 처리
			throw new CustomException(ErrorCode.TOKEN_EXPIRED);

		} catch (JwtException | IllegalArgumentException e) {
			// 유효하지 않은 토큰 처리
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}

	public boolean validateToken(String token) {
		try {
			// 토큰 파싱 시도
			parseClaims(token);
			// 만료 시간 확인
			return true;
		} catch (CustomException e) {
			return false;
		}
	}

	public Duration getLeftExpirationTime(String token) {
		Instant expiration = parseClaims(token).getExpiration().toInstant();
		Instant now = Instant.now();
		return Duration.between(now, expiration);
	}

}
