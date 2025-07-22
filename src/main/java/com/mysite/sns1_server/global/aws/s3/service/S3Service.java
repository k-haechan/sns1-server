package com.mysite.sns1_server.global.aws.s3.service;

import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
	private final S3Presigner presigner;

	private final S3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;


	// images/members/{memberId}/posts/{postId}-{idx}
	public String getPostImagePath(Long memberId, Long postId, int idx) {
		return String.format("images/members/%d/posts/%d-%d", memberId, postId, idx);
	}

	// images/members/{memberId}/posts/*
	public String getPostImagePattern(Long memberId) {
		return String.format("images/members/%d/posts/*", memberId);
	}

	// /images/members/{memberId}/posts/*
	public String getPostImageCookiePath(Long memberId) {
		return String.format("/images/members/%d/posts", memberId);
	}



	public URL generatePresignedUploadUrl(String key) {
		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType("image/webp")
			.build();

		PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(p -> p
			.signatureDuration(Duration.ofMinutes(5))
			.putObjectRequest(objectRequest));

		return presignedRequest.url();
	}

	public void deleteObject(String key) {
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.build();

		try {
			s3Client.deleteObject(deleteObjectRequest);
		} catch (Exception e) {
			log.error("AWS ERROR: s3 Obejct 삭제 에러가 발생하였습니다.");
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}
}
