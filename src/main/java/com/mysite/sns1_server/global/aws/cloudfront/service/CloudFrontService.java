package com.mysite.sns1_server.global.aws.cloudfront.service;

import java.security.PrivateKey;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.mysite.sns1_server.common.util.CookieUtil;
import com.mysite.sns1_server.global.aws.s3.service.S3Service;
import com.mysite.sns1_server.global.config.aws.CloudFrontConfig;
import com.mysite.sns1_server.global.config.common.ServerConfig;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudFrontService {
	private final PrivateKey privateKey;
	private final CloudFrontConfig cloudFrontConfig;
	private final CloudFrontUtilities cloudFrontUtilities;
	private final S3Service s3Service;
	private final ServerConfig serverConfig;

	// Todo: IpRange를 프론트 엔드의 IP로 조절
	public void generateSignedCookies(Long memberId, HttpServletResponse response){ // pattern은 /로 시작해야한다.

		// 1. Signed Cookie 발급
		String pattern = s3Service.getPostImagePattern(memberId);

		String cdnHost = cloudFrontConfig.getCdnHost();

		String keyPairId = cloudFrontConfig.getKeyPairId();

		String resourcePattern = cdnHost + "/" + pattern;

		log.debug("resourcePattern : " + resourcePattern);

		CustomSignerRequest request = CustomSignerRequest.builder()
			.resourceUrl(resourcePattern)
			.resourceUrlPattern(resourcePattern)
			.privateKey(privateKey)
			.keyPairId(keyPairId)
			.activeDate(Instant.now())
			.expirationDate(Instant.now().plusSeconds(CloudFrontConfig.VALID_SECONDS))
			.ipRange("0.0.0.0/0")
			.build();

		CookiesForCustomPolicy cookiesForCustomPolicy = cloudFrontUtilities.getCookiesForCustomPolicy(request);

		// 2. 쿠키에 등록
		setSignedCookie(cookiesForCustomPolicy.signatureHeaderValue(), memberId, response);

		setSignedCookie(cookiesForCustomPolicy.policyHeaderValue(),memberId, response);

		setSignedCookie(cookiesForCustomPolicy.keyPairIdHeaderValue(),memberId, response);
	}

	private void setSignedCookie(String keyValue, Long memberId, HttpServletResponse response) {
		String cookiePath = s3Service.getPostImageCookiePath(memberId);
		String rootDomain = serverConfig.getRootDomain();

		String[] keyValueList = keyValue.split("=");
		String key = keyValueList[0];
		String value = keyValueList[1];

		CookieUtil.setCookie(response,
			key,
			value,
			rootDomain,
			cookiePath,
			CloudFrontConfig.VALID_SECONDS
		);
	}

	public String getCdnHost(){
		return cloudFrontConfig.getCdnHost();
	}
}
