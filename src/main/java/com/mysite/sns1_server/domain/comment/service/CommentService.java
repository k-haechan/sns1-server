package com.mysite.sns1_server.domain.comment.service;

import java.security.Principal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysite.sns1_server.domain.comment.dto.request.CommentRequest;
import com.mysite.sns1_server.domain.comment.dto.response.CommentResponse;
import com.mysite.sns1_server.domain.comment.entity.Comment;
import com.mysite.sns1_server.domain.comment.repository.CommentRepository;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.domain.notification.service.NotificationService;
import com.mysite.sns1_server.domain.notification.type.NotificationType;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.domain.post.repository.PostRepository;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final NotificationService notificationService;
	private final MemberRepository memberRepository;

	public CommentResponse createComment(Principal principal, Long postId, CommentRequest request) {
		// 1. Post 조회
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		// 2. Comment 생성
		Long authorId = Long.parseLong(principal.getName());
		Member author = memberRepository.findById(authorId).orElseThrow(
			() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "Member not found with ID: " + authorId)
		);
		Comment comment = request.toComment(post, author);

		// 3. Comment DB에 저장
		Comment savedComment = commentRepository.save(comment);

		// 4. 알림 생성
		notificationService.createNotification(
			post.getAuthor(),
			NotificationType.COMMENT,
			author.getUsername(),
			post.getId()
		);

		// 4. Comment 응답 정보 생성
		return CommentResponse.from(savedComment);
	}

	@Transactional
	public CommentResponse updateComment(Principal principal, Long commentId, CommentRequest request) {
		// 1. Comment 조회
		Comment comment = commentRepository.findById(commentId).orElseThrow(
			() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

		// 2. 작성자 확인
		Long authorId = Long.parseLong(principal.getName());
		if (!comment.getAuthor().getId().equals(authorId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		// 3. Comment 내용 수정
		comment.updateContent(request.content());

		Comment updatedComment = commentRepository.save(comment);

		return CommentResponse.from(updatedComment);
	}

	public void deleteComment(Principal principal, Long commentId) {
		// 1. Comment 조회
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
		// 2. 작성자 확인
		Long authorId = Long.parseLong(principal.getName());
		if (!comment.getAuthor().getId().equals(authorId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}

		// 3. Comment 삭제
		commentRepository.delete(comment);
	}

	public Slice<CommentResponse> getComments(Principal principal, Long postId, Pageable pageable) {
		// 1. Post 조회
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		// 비공개 게시물은 작성자 본인만 조회 가능(todo)

		// 2. 댓글 목록 조회
		Slice<Comment> comments = commentRepository.findByPostOrderByIdDesc(post, pageable);

		// 3. 댓글 응답 정보 생성
		return comments.map(CommentResponse::from);
	}
}
