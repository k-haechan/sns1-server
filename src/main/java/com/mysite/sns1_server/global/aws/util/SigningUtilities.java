package com.mysite.sns1_server.global.aws.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCannedPolicy;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

public class SigningUtilities {
	private static final Logger logger = LoggerFactory.getLogger(SigningUtilities.class);
	private static final CloudFrontUtilities cloudFrontUtilities = CloudFrontUtilities.create();

	public static SignedUrl signUrlForCannedPolicy(CannedSignerRequest cannedSignerRequest) {
		SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(cannedSignerRequest);
		logger.info("Signed URL: [{}]", signedUrl.url());
		return signedUrl;
	}

	public static SignedUrl signUrlForCustomPolicy(CustomSignerRequest customSignerRequest) {
		SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(customSignerRequest);
		logger.info("Signed URL: [{}]", signedUrl.url());
		return signedUrl;
	}

	public static CookiesForCannedPolicy getCookiesForCannedPolicy(CannedSignerRequest cannedSignerRequest) {
		CookiesForCannedPolicy cookiesForCannedPolicy = cloudFrontUtilities
			.getCookiesForCannedPolicy(cannedSignerRequest);
		logger.info("Cookie EXPIRES header [{}]", cookiesForCannedPolicy.expiresHeaderValue());
		logger.info("Cookie KEYPAIR header [{}]", cookiesForCannedPolicy.keyPairIdHeaderValue());
		logger.info("Cookie SIGNATURE header [{}]", cookiesForCannedPolicy.signatureHeaderValue());
		return cookiesForCannedPolicy;
	}

	public static CookiesForCustomPolicy getCookiesForCustomPolicy(CustomSignerRequest customSignerRequest) {
		CookiesForCustomPolicy cookiesForCustomPolicy = cloudFrontUtilities
			.getCookiesForCustomPolicy(customSignerRequest);
		logger.info("Cookie POLICY header [{}]", cookiesForCustomPolicy.policyHeaderValue());
		logger.info("Cookie KEYPAIR header [{}]", cookiesForCustomPolicy.keyPairIdHeaderValue());
		logger.info("Cookie SIGNATURE header [{}]", cookiesForCustomPolicy.signatureHeaderValue());
		return cookiesForCustomPolicy;
	}
}


