package com.mysite.sns1_server.domain.image.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
import com.mysite.sns1_server.domain.image.entity.Image;
import com.mysite.sns1_server.domain.image.repository.ImageRepository;
import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.entity.Post;
import com.mysite.sns1_server.global.aws.s3.service.S3Service;

@DisplayName("ImageService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ImageService imageService;

    private Member testMember;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().id(1L).build();
        testPost = Post.builder().id(1L).author(testMember).content("Test Post").build();
    }

    @DisplayName("이미지 저장 성공 - 이미지가 있는 경우")
    @Test
    void saveByPost_withImages_success() throws Exception {
        int imagesLength = 2;
        URL mockUrl = new URL("http://mock.presigned.url/image.jpg");

        when(s3Service.getPostImagePath(anyLong(), anyLong(), anyInt())).thenReturn("path/to/image.jpg");
        when(s3Service.generatePresignedUploadUrl(anyString())).thenReturn(mockUrl);
        when(imageRepository.saveAll(anyList())).thenReturn(Collections.emptyList()); // saveAll은 반환값이 중요하지 않으므로 빈 리스트 반환

        List<ImageResponse> result = imageService.saveByPost(testPost, imagesLength);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(imagesLength);
        assertThat(result.get(0).url()).isEqualTo(mockUrl.toString());
        verify(s3Service, times(imagesLength)).getPostImagePath(anyLong(), anyLong(), anyInt());
        verify(s3Service, times(imagesLength)).generatePresignedUploadUrl(anyString());
        verify(imageRepository, times(1)).saveAll(anyList());
    }

    @DisplayName("이미지 저장 성공 - 이미지가 없는 경우")
    @Test
    void saveByPost_noImages_success() {
        int imagesLength = 0;

        List<ImageResponse> result = imageService.saveByPost(testPost, imagesLength);

        assertThat(result).isNull();
        verify(s3Service, never()).getPostImagePath(anyLong(), anyLong(), anyInt());
        verify(s3Service, never()).generatePresignedUploadUrl(anyString());
        verify(imageRepository, never()).saveAll(anyList());
    }

    @DisplayName("게시물로 이미지 찾기 성공")
    @Test
    void findByPost_success() {
        Image image1 = new Image(testPost, "path/to/image1.jpg");
        Image image2 = new Image(testPost, "path/to/image2.jpg");
        List<Image> images = Arrays.asList(image1, image2);

        when(imageRepository.findByPost(any(Post.class))).thenReturn(images);

        List<Image> result = imageService.findByPost(testPost);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPath()).isEqualTo("path/to/image1.jpg");
        verify(imageRepository, times(1)).findByPost(any(Post.class));
    }

    @DisplayName("게시물로 이미지 삭제 성공 - 이미지가 있는 경우")
    @Test
    void deleteImagesByPost_withImages_success() {
        Image image1 = new Image(testPost, "path/to/image1.jpg");
        Image image2 = new Image(testPost, "path/to/image2.jpg");
        List<Image> images = Arrays.asList(image1, image2);

        when(imageRepository.findByPost(any(Post.class))).thenReturn(images);
        doNothing().when(s3Service).deleteObject(anyString());
        doNothing().when(imageRepository).deleteAll(anyList());

        imageService.deleteImagesByPost(testPost);

        verify(imageRepository, times(1)).findByPost(any(Post.class));
        verify(s3Service, times(images.size())).deleteObject(anyString());
        verify(imageRepository, times(1)).deleteAll(anyList());
    }

    @DisplayName("게시물로 이미지 삭제 성공 - 이미지가 없는 경우")
    @Test
    void deleteImagesByPost_noImages_success() {
        when(imageRepository.findByPost(any(Post.class))).thenReturn(Collections.emptyList());

        imageService.deleteImagesByPost(testPost);

        verify(imageRepository, times(1)).findByPost(any(Post.class));
        verify(s3Service, never()).deleteObject(anyString());
        verify(imageRepository, never()).deleteAll(anyList());
    }
}
