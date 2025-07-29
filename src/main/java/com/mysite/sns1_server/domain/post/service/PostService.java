package com.mysite.sns1_server.domain.post.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
import com.mysite.sns1_server.domain.image.service.ImageService;
import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.service.MemberService;
import com.mysite.sns1_server.domain.post.dto.PostRequest;
import com.mysite.sns1_server.domain.post.dto.PostResponse;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.domain.post.repository.PostRepository;
import com.mysite.sns1_server.global.aws.cloudfront.service.CloudFrontService;
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
	private final MemberService memberService;

	/**
	 * 게시물 생성
	 * @param authorId 게시물 작성자 ID
	 * @param request 게시물 요청 정보
	 * @return PostResponse 게시물 응답 정보
	 */
	public PostResponse createPost(Long authorId, PostRequest request) {
		// 1. Post 생성
		Member actor = Member.createActor(authorId);
		Post post = request.toPost(actor);

		// 2. Post DB에 저장
		Post savedPost = postRepository.save(post);

		// 3. 이미지 URL 생성 및 저장(S3에 presigned URL을 통해 생성)
		List<ImageResponse> images = imageService.saveByPost(savedPost, request.imagesLength());

		// 4. 작성자 정보 조회
		MemberBriefResponse members = memberService.getMemberBriefInfo(authorId);

		// 5. 게시물 응답 정보 생성
		return PostResponse.from(savedPost, members, images);
	}


	/**
	 * 게시물 조회
	 * @param memberId 요청하는 사람의 ID
	 * @param postId 조회할 게시물 ID
	 * @return PostResponse 게시물 응답 정보
	 */
	public PostResponse findPost(Long memberId, Long postId) {
		// 1. Post DB에서 조회
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		// 2. Image DB에서 조회 후 실제 데이터 주소로 변환
		List<ImageResponse> images = imageService.findByPost(post);

		// todo: post.author가 비공개계정이라면 팔로우 관계만 볼 수 있음
		// 3. 작성자 정보 조회
		MemberBriefResponse memberBriefInfo = memberService.getMemberBriefInfo(memberId);

		// 4. PostResponse 생성
		return PostResponse.from(post, memberBriefInfo, images);
	}

	/**
	 * 게시물 목록 조회
	 * @param authorId 게시물 작성자의 ID
	 * @param cursorId 커서 ID (null이면 최신 게시물부터 조회)
	 * @param pageable 페이지 정보
	 * @return Slice<PostResponse> 게시물 응답 정보의 슬라이스
	 */
	public Slice<PostResponse> findPosts(Long authorId, Long cursorId, Pageable pageable) {
		// 1. 작성자 정보 조회
		Member author = memberService.findById(authorId);

		// 2. Post DB에서 조회
		Slice<Post> posts = (cursorId == null) ?
			postRepository.findByAuthorOrderByIdDesc(author, pageable) :
			postRepository.findByAuthorAndIdLessThanOrderByIdDesc(author, cursorId, pageable);

		// 3. 조회된 Post를 통해 PostResponse 생성
		return posts.map(post ->
			PostResponse.from(
				post,
				MemberBriefResponse.from(author),
				List.of(ImageResponse.from(post.getThumbnailUrl(cloudFrontService)))
			)
		);
	}

	/**
	 * 게시물 수정
	 * @param memberId 요청하는 사람의 ID
	 * @param postId 수정할 게시물 ID
	 * @param request 게시물 요청 정보
	 * @return PostResponse 수정된 게시물 응답 정보
	 */
	@Transactional
	public PostResponse updatePost(Long memberId, Long postId, PostRequest request) {
		// 1. Post DB에서 찾음
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		// 2. 요청하는 사람의 정보와 게시물 작성자의 정보가 일치하는지 확인
		if(!Objects.equals(post.getAuthor().getId(), memberId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		// 3. 게시글 정보 수정(이미지는 수정x)
		post.updateBy(request);

		// 4. 이미지 URL 생성 및 저장(S3에 presigned URL을 통해 생성)
		List<ImageResponse> images = imageService.findByPost(post);

		// 5. 작성자 정보 조회
		MemberBriefResponse memberBriefInfo = memberService.getMemberBriefInfo(memberId);

		// 6. 반환값 생성
		return PostResponse.from(post, memberBriefInfo, images);
	}

	/**
	 * 게시물 삭제
	 * @param memberId 요청하는 사람의 ID
	 * @param postId 삭제할 게시물 ID
	 */
	public void deletePost(Long memberId, Long postId) {
		// 1. Post DB에서 조회
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
