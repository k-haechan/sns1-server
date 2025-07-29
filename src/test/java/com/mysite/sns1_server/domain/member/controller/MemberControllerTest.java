// package com.mysite.sns1_server.domain.member.controller;
//
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
// import java.util.Collections;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Slice;
// import org.springframework.data.domain.SliceImpl;
// import org.springframework.http.MediaType;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.mysite.sns1_server.domain.member.dto.request.JoinRequest;
// import com.mysite.sns1_server.domain.member.dto.response.MemberBriefResponse;
// import com.mysite.sns1_server.domain.member.dto.response.MemberDetailResponse;
// import com.mysite.sns1_server.domain.member.service.MemberService;
// import com.mysite.sns1_server.domain.post.dto.PostResponse;
// import com.mysite.sns1_server.domain.post.service.PostService;
// import com.mysite.sns1_server.global.aws.cloudfront.service.CloudFrontService;
// import com.mysite.sns1_server.global.config.common.ServerConfig;
// import com.mysite.sns1_server.global.security.jwt.service.JwtService;
//
// import jakarta.servlet.http.HttpServletResponse;
//
// @DisplayName("MemberController 단위테스트")
// @WebMvcTest(MemberController.class)
// @AutoConfigureMockMvc(addFilters = false)
// class MemberControllerTest {
//
//     @Autowired
//     private MockMvc mockMvc;
//
//     @MockitoBean
//     private MemberService memberService;
//
//     @MockitoBean
//     private PasswordEncoder passwordEncoder;
//
//     @MockitoBean
//     private JwtService jwtService;
//
//     @MockitoBean
//     private ServerConfig serverConfig;
//
//     @MockitoBean
//     private PostService postService;
//
//     @MockitoBean
//     private CloudFrontService cloudFrontService;
//
//     @MockitoBean
//     private UserDetailsService userDetailsService;
//
//
//
//     @Autowired
//     private ObjectMapper objectMapper;
//
//     @DisplayName("signUp: 회원가입 성공")
//     @Test
//     void t1() throws Exception {
//         // given
//         JoinRequest joinRequest = new JoinRequest("testUser", "password", "테스트 유저", "test@example.com");
//         doNothing().when(memberService).join(any(JoinRequest.class));
//
//         // when, then
//         mockMvc.perform(post("/api/v1/members/join")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(joinRequest)))
//                 .andExpect(status().isCreated());
//     }
//
//     @DisplayName("signUp: 유효성 검증 실패")
//     @Test
//     void t2() throws Exception {
//         // given
//         JoinRequest joinRequest = new JoinRequest("", "", "", "");
//
//         // when, then
//         mockMvc.perform(post("/api/v1/members/join")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(joinRequest)))
//                 .andExpect(status().isBadRequest());
//     }
//
//     @DisplayName("getMemberInfo: 회원 정보 조회 성공")
//     @Test
//     void getMemberInfo_success() throws Exception {
//         // given
//         Long memberId = 1L;
//         MemberDetailResponse memberDetailResponse = new MemberDetailResponse(memberId, "testUser", "테스트 유저", "profile.jpg", "intro", 10L, 20L);
//         when(memberService.getMemberInfo(memberId)).thenReturn(memberDetailResponse);
//
//         // when, then
//         mockMvc.perform(get("/api/v1/members/{member_id}", memberId))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data.member_id").value(memberId))
//                 .andExpect(jsonPath("$.data.username").value("testUser"))
//                 .andExpect(jsonPath("$.data.real_name").value("테스트 유저"));
//     }
//
//     @DisplayName("searchMemberByName: 회원 이름으로 검색 성공")
//     @Test
//     void searchMemberByName_success() throws Exception {
//         // given
//         String username = "testUser";
//         MemberBriefResponse memberBriefResponse = new MemberBriefResponse(1L, "testUser", "테스트 유저", "profile.jpg");
//         when(memberService.searchMemberByUsername(username)).thenReturn(memberBriefResponse);
//
//         // when, then
//         mockMvc.perform(get("/api/v1/members").param("username", username))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data.member_id").value(1L))
//                 .andExpect(jsonPath("$.data.username").value("testUser"));
//     }
//
//     @DisplayName("getMemberPosts: 회원 게시글 조회 성공")
//     @Test
//     void getMemberPosts_success() throws Exception {
//         // given
//         Long memberId = 1L;
//         Pageable pageable = PageRequest.of(0, 10);
//         PostResponse mockPostResponse = new PostResponse(1L, "title", "content", Collections.emptyList());
//         Slice<PostResponse> mockSlice = new SliceImpl<>(Collections.singletonList(mockPostResponse), pageable, false);
//
//         when(postService.findPosts(any(Long.class), nullable(Long.class), any(Pageable.class))).thenReturn(mockSlice);
//         doNothing().when(cloudFrontService).generateSignedCookies(eq(memberId), any(HttpServletResponse.class));
//
//         // when, then
//         mockMvc.perform(get("/api/v1/members/{member_id}/posts", memberId)
//                 .param("page", "0")
//                 .param("size", "10"))
//                 .andDo(print())
//                 .andExpect(status().isOk())
//                 .andExpect(content().json("{\"message\":\"회원의 게시물 조회가 성공적으로 완료되었습니다.\",\"data\":{\"content\":[{\"post_id\":1,\"title\":\"title\",\"content\":\"content\",\"images\":[]}]}}"));
//     }
// }
