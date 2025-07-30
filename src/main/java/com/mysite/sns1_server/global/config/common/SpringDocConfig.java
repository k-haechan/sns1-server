package com.mysite.sns1_server.global.config.common;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@Profile({"dev", "prod"}) // 필요 시 설정
public class SpringDocConfig {

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Bean
	public OpenAPI customOpenAPI() {
		Server server;

		if ("prod".equals(activeProfile)) {
			server = new Server().url("https://api.sns1.haechan.site").description("배포 서버");
		} else {
			server = new Server().url("http://localhost:8080").description("로컬 개발 서버");
		}

		return new OpenAPI()
			.info(new Info()
				.title("API 서버")
				.version("v1")
			)
			.servers(List.of(server));
	}

	@Bean
	public GroupedOpenApi groupApiV1() {
		return GroupedOpenApi.builder()
			.group("api-v1")
			.pathsToMatch("/api/v1/**")
			.build();
	}
}
