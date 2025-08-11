package com.mysite.sns1_server.domain.comment.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.mysite.sns1_server.domain.comment.dto.request.CommentRequest;
import com.mysite.sns1_server.domain.comment.dto.response.CommentResponse;
import com.mysite.sns1_server.domain.comment.entity.Comment;
import com.mysite.sns1_server.domain.comment.repository.CommentRepository;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.member.repository.MemberRepository;
import com.mysite.sns1_server.domain.notification.service.NotificationService;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.domain.post.repository.PostRepository;
import com.mysite.sns1_server.global.exception.CustomException;
import com.mysite.sns1_server.global.response.code.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    private Principal createPrincipal(Long memberId) {
        return () -> String.valueOf(memberId);
    }

    private Member createMember(Long id) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Post createPost(Long id, Member author) {
        Post post = new Post(author, "Test Title", "Test Content");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Comment createComment(Long id, Post post, Member author, String content) {
        Comment comment = Comment.create(content, post, author);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("성공")
        void createCommentSuccess() {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            Principal principal = createPrincipal(memberId);
            CommentRequest request = new CommentRequest("Test Comment");

            Member author = createMember(memberId);
            Post post = createPost(postId, author);
            Comment comment = createComment(1L, post, author, request.content());

            given(memberRepository.findById(memberId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            // when
            CommentResponse response = commentService.createComment(principal, postId, request);

            // then
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.postId()).isEqualTo(postId);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("실패 - 게시글을 찾을 수 없음")
        void createCommentFailPostNotFound() {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            Principal principal = createPrincipal(memberId);
            CommentRequest request = new CommentRequest("Test Comment");

            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.createComment(principal, postId, request));
            assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("성공")
        void updateCommentSuccess() {
            // given
            Long memberId = 1L;
            Long commentId = 1L;
            Principal principal = createPrincipal(memberId);
            CommentRequest request = new CommentRequest("Updated Comment");

            Member author = createMember(memberId);
            Post post = createPost(1L, author);
            Comment comment = createComment(commentId, post, author, "Original Comment");

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            // when
            CommentResponse response = commentService.updateComment(principal, commentId, request);

            // then
            assertThat(response.content()).isEqualTo(request.content());
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("실패 - 댓글을 찾을 수 없음")
        void updateCommentFailCommentNotFound() {
            // given
            Long memberId = 1L;
            Long commentId = 1L;
            Principal principal = createPrincipal(memberId);
            CommentRequest request = new CommentRequest("Updated Comment");

            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.updateComment(principal, commentId, request));
            assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updateCommentFailForbidden() {
            // given
            Long memberId = 1L;
            Long anotherMemberId = 2L;
            Long commentId = 1L;
            Principal principal = createPrincipal(anotherMemberId);
            CommentRequest request = new CommentRequest("Updated Comment");

            Member author = createMember(memberId);
            Post post = createPost(1L, author);
            Comment comment = createComment(commentId, post, author, "Original Comment");

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.updateComment(principal, commentId, request));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("성공")
        void deleteCommentSuccess() {
            // given
            Long memberId = 1L;
            Long commentId = 1L;
            Principal principal = createPrincipal(memberId);

            Member author = createMember(memberId);
            Post post = createPost(1L, author);
            Comment comment = createComment(commentId, post, author, "Test Comment");

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when
            commentService.deleteComment(principal, commentId);

            // then
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("실패 - 댓글을 찾을 수 없음")
        void deleteCommentFailCommentNotFound() {
            // given
            Long memberId = 1L;
            Long commentId = 1L;
            Principal principal = createPrincipal(memberId);

            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.deleteComment(principal, commentId));
            assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deleteCommentFailForbidden() {
            // given
            Long memberId = 1L;
            Long anotherMemberId = 2L;
            Long commentId = 1L;
            Principal principal = createPrincipal(anotherMemberId);

            Member author = createMember(memberId);
            Post post = createPost(1L, author);
            Comment comment = createComment(commentId, post, author, "Test Comment");

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.deleteComment(principal, commentId));
            assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("성공")
        void getCommentsSuccess() {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            Member author = createMember(memberId);
            Post post = createPost(postId, author);
            Comment comment = createComment(1L, post, author, "Test Comment");
            Slice<Comment> comments = new SliceImpl<>(List.of(comment), pageable, false);

            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(commentRepository.findByPostOrderByIdDesc(post, pageable)).willReturn(comments);

            // when
            Slice<CommentResponse> response = commentService.getComments(principal, postId, pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).content()).isEqualTo("Test Comment");
        }

        @Test
        @DisplayName("실패 - 게시글을 찾을 수 없음")
        void getCommentsFailPostNotFound() {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            Principal principal = createPrincipal(memberId);
            Pageable pageable = PageRequest.of(0, 10);

            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> commentService.getComments(principal, postId, pageable));
            assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
        }
    }
}
