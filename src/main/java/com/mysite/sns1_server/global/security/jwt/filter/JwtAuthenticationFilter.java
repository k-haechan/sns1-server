package com.mysite.sns1_server.global.security.jwt.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mysite.sns1_server.common.util.CookieUtil;
import com.mysite.sns1_server.global.security.jwt.service.AccessTokenService;
import com.mysite.sns1_server.global.security.jwt.service.RefreshTokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final AccessTokenService accessTokenService;
	private final RefreshTokenService refreshTokenService;

	private void setAuthentication(Long memberId) {
		// 인증 객체 생성 및 SecurityContext에 저장
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			memberId,
			null,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}


	/**
	 * JWT 인증 필터: 요청마다 실행되며, access-token이 존재하면 사용자 인증을 수행합니다.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		// token의 이름을 가져옴
		String accessTokenName = accessTokenService.getTokenName();
		String refreshTokenName = refreshTokenService.getTokenName();

		// 쿠키에서 access-token 추출
		String accessToken = CookieUtil.extractCookie(request, accessTokenName);
		String refreshToken = CookieUtil.extractCookie(request, refreshTokenName);

		try {
			if (accessToken != null) {
					// JWT 파싱 및 claims 추출
					Claims claims = accessTokenService.parseClaims(accessToken);
					Long memberId = Long.valueOf(claims.getSubject());
					setAuthentication(memberId);

			} else throw new JwtException("Access token is missing");
		} catch (JwtException e) { // 토큰 검증 실패 시
			// refresh-token 검증
			if (refreshToken != null) {
				boolean validated = refreshTokenService.validateToken(refreshToken);
				// 유효한 토큰인 경우 새로운 access-token 발급
				if (validated) {
					Claims claims = refreshTokenService.parseClaims(refreshToken);
					Long memberId = Long.valueOf(claims.getSubject());
					// 새로운 access-token 발급
					String newAccessToken = accessTokenService.generateToken(memberId);
					// 쿠키에 새로운 access-token 설정
					CookieUtil.setCookie(response, accessTokenName, newAccessToken, accessTokenService.getExpiration());
					// 인증 객체 생성 및 SecurityContext에 저장
					setAuthentication(memberId);
				}
			}
		}
		// 다음 필터로 요청 전달
		filterChain.doFilter(request, response);
	}
}
