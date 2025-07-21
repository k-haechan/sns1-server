package com.mysite.sns1_server.domain.post.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sns1_server.domain.image.service.ImageService;
import com.mysite.sns1_server.domain.post.dto.PostRequest;
import com.mysite.sns1_server.domain.post.dto.PostResponse;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.domain.post.service.PostService;
import com.mysite.sns1_server.global.aws.cloudfront.service.CloudFrontService;
import com.mysite.sns1_server.global.aws.s3.service.S3Service;
import com.mysite.sns1_server.global.response.CustomResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "Post", description = "게시물 관련 API")
public class PostController {
	private final PostService postService;
	private final ImageService imageService;
	private final CloudFrontService cloudFrontService;
	private final S3Service s3Service;

	@Value("${server.root.domain}")
	String rootDomain;

	/* 게시물 업로드 */
	@PostMapping
	@Operation(summary = "게시물 생성 및 요청", description = "게시물을 생성합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<PostResponse> generatePost(
		Principal principal,
		@Valid @RequestBody PostRequest request
	) {
		Long memberId = Long.valueOf(principal.getName());
		PostResponse result = postService.createPost(memberId, request);

		return CustomResponseBody.of("게시물 데이터 생성 완료", result);
	}

	/* 게시물 조회 */
	@GetMapping("/{post-id}")
	@Operation(summary = "게시물 조회", description = "게시물을 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<PostResponse> getPost(
		Principal principal,
		@PathVariable("post-id") Long postId,
		HttpServletResponse response
	) {
		Long memberId = Long.valueOf(principal.getName());

		// 게시물 조회
		PostResponse result = postService.findPost(memberId, postId);

		// 접근할 수 있는 Cookie 발급
		cloudFrontService.generateSignedCookies(memberId, response);

		log.debug(response.toString());

		return CustomResponseBody.of("게시물이 성공적으로 반환되었습니다.", result);
	}

	/* 게시물 수정 */
	@PutMapping("/{post-id}")
	@Operation(summary = "게시물 수정", description = "게시물을 수정합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<PostResponse> updatePost(
		Principal principal,
		@PathVariable("post-id") Long postId,
		PostRequest request
	) {
		Long memberId = Long.valueOf(principal.getName());
		Post updatedPost = postService.updatePost(memberId, postId, request);

		return CustomResponseBody.of("게시물 데이터 수정 완료", PostResponse.from(updatedPost, null));
	}


	/* 게시물 삭제 */
	@DeleteMapping("/{post-id}")
	@Operation(summary = "게시물 삭제", description = "게시물을 삭제합니다.")
	@ResponseStatus(HttpStatus.OK)
	public CustomResponseBody<PostResponse> deletePost(
		Principal principal,
		@PathVariable("post-id") Long postId
	) {
		Long memberId = Long.valueOf(principal.getName());
		postService.deletePost(memberId, postId);

		return CustomResponseBody.of("게시물 데이터 삭제 완료", null);
	}

}
