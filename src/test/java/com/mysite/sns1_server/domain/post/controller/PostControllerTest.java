// package com.mysite.sns1_server.domain.post.controller;
//
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.security.Principal;
// import java.util.Arrays;
// import java.util.List;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.mysite.sns1_server.domain.image.dto.response.ImageResponse;
// import com.mysite.sns1_server.domain.image.service.ImageService;
// import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
// import com.mysite.sns1_server.domain.member.entity.Member;
// import com.mysite.sns1_server.domain.post.dto.PostRequest;
// import com.mysite.sns1_server.domain.post.dto.PostResponse;
// import com.mysite.sns1_server.domain.post.entity.Post;
// import com.mysite.sns1_server.domain.post.service.PostService;
// import com.mysite.sns1_server.global.aws.cloudfront.service.CloudFrontService;
// import com.mysite.sns1_server.global.aws.s3.service.S3Service;
//
// import jakarta.servlet.http.HttpServletResponse;
//
// @DisplayName("PostController 단위 테스트")
// @WebMvcTest(PostController.class)
// @AutoConfigureMockMvc(addFilters = false)
// class PostControllerTest {
//
//     @Autowired
//     private MockMvc mockMvc;
//
//     @Autowired
//     private ObjectMapper objectMapper;
//
//     @MockitoBean
//     private PostService postService;
//
//     @MockitoBean
//     private ImageService imageService;
//
//     @MockitoBean
//     private CloudFrontService cloudFrontService;
//
//     @MockitoBean
//     private S3Service s3Service;
//
//     private Principal mockPrincipal;
//     private Member testMember;
//     private MemberBriefResponse testMemberBriefResponse;
//     private Post testPost;
//     private PostRequest testPostRequest;
//     private PostResponse testPostResponse;
//
//     @BeforeEach
//     void setUp() {
//         mockPrincipal = () -> "1"; // Mock Principal with memberId 1
//         testMember = Member.builder().id(1L).build();
//         testPost = Post.builder().id(1L).author(testMember).content("Test Post").build();
//         testMemberBriefResponse = MemberBriefResponse.from(testMember);
//         testPostRequest = new PostRequest("Test title", "Test content", 5);
//         List<ImageResponse> images = Arrays.asList(ImageResponse.from("image1.jpg"), ImageResponse.from("image2.jpg"));
//         testPostResponse = PostResponse.from(testPost, testMemberBriefResponse, images);
//     }
//
//     @DisplayName("게시물 생성 API 성공")
//     @Test
//     void generatePost_success() throws Exception {
//         when(postService.createPost(anyLong(), any(PostRequest.class))).thenReturn(testPostResponse);
//
//         mockMvc.perform(post("/api/v1/posts")
//                 .principal(mockPrincipal)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(testPostRequest)))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$.message").value("게시물 데이터 생성 완료"))
//             .andExpect(jsonPath("$.data.post_id").value(testPost.getId()));
//
//         verify(postService, times(1)).createPost(anyLong(), any(PostRequest.class));
//     }
//
//     @DisplayName("게시물 조회 API 성공")
//     @Test
//     void getPost_success() throws Exception {
//         when(postService.findPost(anyLong(), anyLong())).thenReturn(testPostResponse);
//         doNothing().when(cloudFrontService).generateSignedCookies(anyLong(), any(HttpServletResponse.class));
//
//         mockMvc.perform(get("/api/v1/posts/{post-id}", testPost.getId())
//                 .principal(mockPrincipal))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$.message").value("게시물이 성공적으로 반환되었습니다."))
//             .andExpect(jsonPath("$.data.post_id").value(testPost.getId()));
//
//         verify(postService, times(1)).findPost(anyLong(), anyLong());
//         verify(cloudFrontService, times(1)).generateSignedCookies(anyLong(), any(HttpServletResponse.class));
//     }
//
//     @DisplayName("게시물 수정 API 성공")
//     @Test
//     void updatePost_success() throws Exception {
//         Post updatedPost = Post.builder().id(1L).author(testMember).content("Updated Content").build();
//         when(postService.updatePost(anyLong(), anyLong(), any(PostRequest.class))).thenReturn(PostResponse.from(updatedPost, testMemberBriefResponse, List.of()));
//
//         mockMvc.perform(put("/api/v1/posts/{post-id}", testPost.getId())
//                 .principal(mockPrincipal)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(new PostRequest("Updated Title","Updated Content", 0))))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$.message").value("게시물 데이터 수정 완료"))
//             .andExpect(jsonPath("$.data.content").value("Updated Content"));
//
//         verify(postService, times(1)).updatePost(anyLong(), anyLong(), any(PostRequest.class));
//     }
//
//     @DisplayName("게시물 삭제 API 성공")
//     @Test
//     void deletePost_success() throws Exception {
//         doNothing().when(postService).deletePost(anyLong(), anyLong());
//
//         mockMvc.perform(delete("/api/v1/posts/{post-id}", testPost.getId())
//                 .principal(mockPrincipal))
//             .andExpect(status().isOk())
//             .andExpect(jsonPath("$.message").value("게시물 데이터 삭제 완료"));
//
//         verify(postService, times(1)).deletePost(anyLong(), anyLong());
//     }
// }
