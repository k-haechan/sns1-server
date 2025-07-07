package com.mysite.sns1_server.global.config;

import static org.springframework.http.HttpMethod.*;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mysite.sns1_server.global.security.exception.CustomAccessDeniedHandler;
import com.mysite.sns1_server.global.security.exception.CustomAuthenticationEntryPoint;
import com.mysite.sns1_server.global.security.jwt.filter.JwtAuthenticationFilter;
import com.mysite.sns1_server.global.security.jwt.service.AccessTokenService;
import com.mysite.sns1_server.global.security.jwt.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final ServerConfig serverConfig;
	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// ✅ 보안 관련 설정 (CSRF, CORS, 세션)
			.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (JWT 사용 시 불필요)
			.cors(cors -> corsConfigurationSource()) // CORS 설정 적용
			.sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT 방식)

			// ✅ 인증 및 접근 권한 설정
			.authorizeHttpRequests(authorize -> authorize

				// Swagger 관련 URL 접근 허용
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
					"/swagger-ui.html").permitAll()

				// MEMBER Domain
				.requestMatchers(POST, "/api/v1/members/join").permitAll()
				.requestMatchers(POST, "/api/v1/auth/**").permitAll()
				.requestMatchers(OPTIONS, "/**").permitAll()
				.requestMatchers("/error").permitAll()
				.anyRequest().authenticated() // 나머지 요청은 인증 필요
			)

			// ✅ JWT 필터 등록
			.addFilterBefore(
				new JwtAuthenticationFilter(accessTokenService, refreshTokenService),
				UsernamePasswordAuthenticationFilter.class)

			// ✅ 기본 인증 방식 비활성화 (JWT 사용)
			.httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
			.formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화

			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
				.accessDeniedHandler(new CustomAccessDeniedHandler())
			);
		return http.build();
	}

	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// 허용할 오리진 설정
		configuration.setAllowedOrigins(
			serverConfig.getFrontendUrl()); // 프론트 엔드
		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // 프론트 엔드 허용 메서드
		// 자격 증명 허용 설정
		configuration.setAllowCredentials(true);
		// 허용할 헤더 설정
		configuration.setAllowedHeaders(Collections.singletonList("*"));

		configuration.setExposedHeaders(
			List.of("Set-Cookie"));

		// CORS 설정을 소스에 등록
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}

