package com.mysite.sns1_server.global.security.jwt.service;

import java.time.Duration;

import javax.crypto.SecretKey;

public class RefreshTokenService extends JwtService{
	public RefreshTokenService(String tokenName,Duration expiration, SecretKey secretKey) {
		super(tokenName, expiration, secretKey);
	}
}
