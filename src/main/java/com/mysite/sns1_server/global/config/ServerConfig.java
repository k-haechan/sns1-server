package com.mysite.sns1_server.global.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class ServerConfig {
	@Value("${custom.server.frontend.url}")
	private List<String> frontendUrl;
}
