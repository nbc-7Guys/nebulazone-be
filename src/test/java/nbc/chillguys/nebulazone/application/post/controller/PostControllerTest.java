package nbc.chillguys.nebulazone.application.post.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.SearchPostResponse;
import nbc.chillguys.nebulazone.application.post.service.PostService;
import nbc.chillguys.nebulazone.config.TestMockConfig;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.infra.security.filter.JwtAuthenticationFilter;
import nbc.chillguys.nebulazone.support.mockuser.WithCustomMockUser;

@Import({TestSecurityConfig.class, TestMockConfig.class})
@DisplayName("게시글 컨트롤러 단위 테스트")
@WebMvcTest(
	controllers = PostController.class,
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
			JwtAuthenticationFilter.class
		})
	}
)
class PostControllerTest {
	@MockitoBean
	private PostService postService;

	@Autowired
	private MockMvc mockMvc;

	@Nested
	@DisplayName("게시글 생성")
	class CreatePostTest {

		@Test
		@DisplayName("게시글 생성 성공")
		@WithCustomMockUser
		void success_createPost() {
			// 다시 짜야함
		}
	}

	@Nested
	@DisplayName("게시글 검색")
	class SearchPostTest {

		@Test
		@DisplayName("게시글 검색 성공")
		void success_searchPost() throws Exception {
			// Given
			SearchPostResponse response = new SearchPostResponse(1L, "title", "content", "FREE", 1L, "user",
				LocalDateTime.now(), List.of());
			Page<SearchPostResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

			given(postService.searchPost(anyString(), any(), anyInt(), anyInt())).willReturn(page);

			// When
			ResultActions perform = mockMvc.perform(
				get("/posts").param("keyword", "test").param("type", "FREE").param("page", "1").param("size", "10"));

			// Then
			perform.andDo(print())
				.andExpectAll(status().isOk(), jsonPath("$.content[0].postId").value(1L),
					jsonPath("$.content[0].title").value("title"),
					jsonPath("$.content[0].content").value("content"),
					jsonPath("$.content[0].type").value("FREE"),
					jsonPath("$.totalElements").value(1));
		}
	}

	@Nested
	@DisplayName("게시글 조회")
	class GetPostTest {

		@Test
		@DisplayName("게시글 조회 성공")
		void success_getPost() throws Exception {
			// Given
			Long postId = 1L;
			GetPostResponse response = new GetPostResponse(postId, "title", "content", "FREE", 1L, "user",
				LocalDateTime.now(), List.of());
			given(postService.getPost(postId)).willReturn(response);

			// When
			ResultActions perform = mockMvc.perform(get("/posts/{postId}", postId));

			// Then
			perform.andDo(print())
				.andExpectAll(status().isOk(),
					jsonPath("$.postId").value(postId),
					jsonPath("$.title").value("title"),
					jsonPath("$.content").value("content"));
		}
	}

}
