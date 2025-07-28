package com.mysite.sns1_server.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

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

@DisplayName("PostService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CloudFrontService cloudFrontService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostService postService;

    private Member testMember;
    private Post testPost;
    private PostRequest testPostRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().id(1L).build();
        testPost = Post.builder().id(1L).author(testMember).content("Test Post").build();
        testPostRequest = new PostRequest("Test Title","Test Content", 2);
    }

    @DisplayName("게시물 생성 성공")
    @Test
    void createPost_success() {
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(imageService.saveByPost(any(Post.class), anyInt()))
            .thenReturn(Arrays.asList(ImageResponse.from("image1.jpg"), ImageResponse.from("image2.jpg")));

        PostResponse result = postService.createPost(testMember.getId(), testPostRequest);

        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(testPost.getId());
        assertThat(result.images()).hasSize(2);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(imageService, times(1)).saveByPost(any(Post.class), anyInt());
    }

    @DisplayName("게시물 조회 성공")
    @Test
    void findPost_success() {
        Image image1 = new Image(testPost, "path/to/image1.jpg");
        Image image2 = new Image(testPost, "path/to/image2.jpg");
        List<Image> images = Arrays.asList(image1, image2);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(imageService.findByPost(any(Post.class))).thenReturn(images);
        when(cloudFrontService.getCdnHost()).thenReturn("http://cdn.example.com");

        PostResponse result = postService.findPost(testMember.getId(), testPost.getId());

        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(testPost.getId());
        assertThat(result.images()).hasSize(2);
        assertThat(result.images().get(0).url()).startsWith("http://cdn.example.com");
        verify(postRepository, times(1)).findById(anyLong());
        verify(imageService, times(1)).findByPost(any(Post.class));
        verify(cloudFrontService, times(1)).getCdnHost();
    }

    @DisplayName("게시물 조회 실패 - 게시물 없음")
    @Test
    void findPost_notFound() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findPost(testMember.getId(), 999L))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(ErrorCode.POST_NOT_FOUND.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
        verify(imageService, never()).findByPost(any(Post.class));
    }

    @DisplayName("게시물 목록 조회 성공 - cursorId 없음")
    @Test
    void findPosts_noCursorId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Post post2 = Post.builder().id(2L).author(testMember).content("Test Post 2").build();
        SliceImpl<Post> postsSlice = new SliceImpl<>(Arrays.asList(testPost, post2), pageable, true);

        when(postRepository.findByAuthorOrderByIdDesc(any(Member.class), any(Pageable.class)))
            .thenReturn(postsSlice);
        when(cloudFrontService.getPostImagePath(eq(testMember), any(Post.class), eq(0))).thenReturn("http://s3.example.com/thumbnail.jpg");

        SliceImpl<PostResponse> result = (SliceImpl<PostResponse>) postService.findPosts(testMember.getId(), null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).postId()).isEqualTo(testPost.getId());
        assertThat(result.getContent().get(0).images().get(0).url()).isEqualTo("http://s3.example.com/thumbnail.jpg");
        verify(postRepository, times(1)).findByAuthorOrderByIdDesc(any(Member.class), any(Pageable.class));
        verify(postRepository, never()).findByAuthorAndIdLessThanOrderByIdDesc(any(Member.class), anyLong(), any(Pageable.class));
    }

    @DisplayName("게시물 목록 조회 성공 - cursorId 있음")
    @Test
    void findPosts_withCursorId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Post post2 = Post.builder().id(2L).author(testMember).content("Test Post 2").build();
        SliceImpl<Post> postsSlice = new SliceImpl<>(Arrays.asList(post2), pageable, false);

        when(postRepository.findByAuthorAndIdLessThanOrderByIdDesc(any(Member.class), anyLong(), any(Pageable.class)))
            .thenReturn(postsSlice);
        when(cloudFrontService.getPostImagePath(eq(testMember), any(Post.class), eq(0))).thenReturn("http://s3.example.com/thumbnail.jpg");

        SliceImpl<PostResponse> result = (SliceImpl<PostResponse>) postService.findPosts(testMember.getId(), 3L, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).postId()).isEqualTo(post2.getId());
        assertThat(result.getContent().get(0).images().get(0).url()).isEqualTo("http://s3.example.com/thumbnail.jpg");
        verify(postRepository, never()).findByAuthorOrderByIdDesc(any(Member.class), any(Pageable.class));
        verify(postRepository, times(1)).findByAuthorAndIdLessThanOrderByIdDesc(any(Member.class), anyLong(), any(Pageable.class));
    }

    @DisplayName("게시물 수정 성공")
    @Test
    void updatePost_success() {
        PostRequest updateRequest = new PostRequest("Updated Title","Updated Content", 0);
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        Post updatedPost = postService.updatePost(testMember.getId(), testPost.getId(), updateRequest);

        assertThat(updatedPost).isNotNull();
        assertThat(updatedPost.getContent()).isEqualTo("Updated Content");
        verify(postRepository, times(1)).findById(anyLong());
    }

    @DisplayName("게시물 수정 실패 - 게시물 없음")
    @Test
    void updatePost_notFound() {
        PostRequest updateRequest = new PostRequest("Updated Title","Updated Content", 0);
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(testMember.getId(), 999L, updateRequest))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(ErrorCode.POST_NOT_FOUND.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
    }

    @DisplayName("게시물 수정 실패 - 권한 없음")
    @Test
    void updatePost_forbidden() {
        Member otherMember = Member.builder().id(2L).build();
        Post otherPost = Post.builder().id(2L).author(otherMember).content("Other Post").build();
        PostRequest updateRequest = new PostRequest("Updated Title","Updated Content", 0);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(otherPost));

        assertThatThrownBy(() -> postService.updatePost(testMember.getId(), otherPost.getId(), updateRequest))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
    }

    @DisplayName("게시물 삭제 성공")
    @Test
    void deletePost_success() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        doNothing().when(imageService).deleteImagesByPost(any(Post.class));
        doNothing().when(postRepository).deleteById(anyLong());

        postService.deletePost(testMember.getId(), testPost.getId());

        verify(postRepository, times(1)).findById(anyLong());
        verify(imageService, times(1)).deleteImagesByPost(any(Post.class));
        verify(postRepository, times(1)).deleteById(anyLong());
    }

    @DisplayName("게시물 삭제 실패 - 게시물 없음")
    @Test
    void deletePost_notFound() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(testMember.getId(), 999L))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(ErrorCode.POST_NOT_FOUND.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
        verify(imageService, never()).deleteImagesByPost(any(Post.class));
        verify(postRepository, never()).deleteById(anyLong());
    }

    @DisplayName("게시물 삭제 실패 - 권한 없음")
    @Test
    void deletePost_forbidden() {
        Member otherMember = Member.builder().id(2L).build();
        Post otherPost = Post.builder().id(2L).author(otherMember).content("Other Post").build();

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(otherPost));

        assertThatThrownBy(() -> postService.deletePost(testMember.getId(), otherPost.getId()))
            .isInstanceOf(CustomException.class)
            .hasMessageContaining(ErrorCode.FORBIDDEN.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
        verify(imageService, never()).deleteImagesByPost(any(Post.class));
        verify(postRepository, never()).deleteById(anyLong());
    }
}
