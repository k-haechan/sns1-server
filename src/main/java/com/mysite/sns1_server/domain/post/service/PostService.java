package com.mysite.sns1_server.domain.post.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
import com.mysite.sns1_server.domain.image.entity.Image;
import com.mysite.sns1_server.domain.image.service.ImageService;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.dto.PostRequest;
import com.mysite.sns1_server.domain.post.dto.PostResponse;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.domain.post.repository.PostRepository;
import com.mysite.sns1_server.global.aws.cloudfront.service.CloudFrontService;
import com.mysite.sns1_server.global.aws.s3.service.S3Service;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
	private final ImageService imageService;
	private final PostRepository postRepository;
	private final CloudFrontService cloudFrontService;
	private final S3Service s3Service;

	public PostResponse createPost(Long authorId, PostRequest request) {
		Member actor = Member.createActor(authorId);
		Post post = request.toPost(actor);

		Post savedPost = postRepository.save(post);

		List<ImageResponse> images = imageService.saveByPost(savedPost, request.imagesLength());

		return PostResponse.from(savedPost, images);
	}

	public PostResponse findPost(Long memberId, Long postId) {
		// 1. Post DB에서 찾음
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		// 2. Image DB에서 찾은 후 실제 데이터 주소로 변환
		List<Image> images = imageService.findByPost(post);
		String cdnHost = cloudFrontService.getCdnHost();
		List<ImageResponse> imageResponses = images.stream()
			.map(image -> ImageResponse.from(image, cdnHost)).collect(
			Collectors.toList());

		// todo: post.author가 비공개계정이라면 팔로우 관계만 볼 수 있음

		return PostResponse.from(post, imageResponses);
	}

	public Slice<PostResponse> findPosts(Long memberId, Long cursorId, Pageable pageable) {
		Member author = Member.createActor(memberId);

		Slice<Post> posts = (cursorId == null) ?
			postRepository.findByAuthorOrderByIdDesc(author, pageable) :
			postRepository.findByAuthorAndIdLessThanOrderByIdDesc(author, cursorId, pageable);

		return posts.map(post -> PostResponse.from(post, List.of(ImageResponse.from(post.getThumbnailUrl(cloudFrontService)))));
	}


	@Transactional
	public Post updatePost(Long memberId, Long postId, PostRequest request) {
		// 1. Post DB에서 찾음
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		log.error("memberId : " + memberId);
		log.error("authorId : " + post.getAuthor().getId());
		// 2. 요청하는 사람의 정보와 게시물 작성자의 정보가 일치하는지 확인
		if(!Objects.equals(post.getAuthor().getId(), memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		// 3. 게시글 정보 수정(이미지는 수정x)
		post.updateBy(request);

		return post;
	}

	public void deletePost(Long memberId, Long postId) {
		// 1. Post DB에서 찾음
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		// 2. 요청하는 사람의 정보와 게시물 작성자의 정보가 일치하는지 확인
		if(!Objects.equals(post.getAuthor().getId(), memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		// 3. images 삭제, s3의 값도 같이 삭제
		imageService.deleteImagesByPost(post);

		// 4. Post 삭제
		postRepository.deleteById(postId);
	}
}
