package com.mysite.sns1_server.global.aws;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
public class SigningService {
	/* Create a pre-signed URL to download an object in a subsequent GET request. */
	public String createPresignedGetUrl(String bucketName, String keyName) {
		try (S3Presigner presigner = S3Presigner.create()) {

			GetObjectRequest objectRequest = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(keyName)
				.build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
				.getObjectRequest(objectRequest)
				.build();

			PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
			log.info("Presigned URL: [{}]", presignedRequest.url().toString());
			log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

			return presignedRequest.url().toExternalForm();
		}
	}

}
