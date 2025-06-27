package nbc.chillguys.nebulazone.application.review.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminSearchRequest;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewAdminResponse;
import nbc.chillguys.nebulazone.application.review.service.ReviewAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.config.TestSecurityConfig;
import nbc.chillguys.nebulazone.support.MockMvc.TestMockConfig;

@WebMvcTest(ReviewAdminController.class)
@Import({TestSecurityConfig.class, TestMockConfig.class})
class ReviewAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ReviewAdminService reviewAdminService;

	@Test
	@DisplayName("리뷰리스트 찾기 성공")
	void success_findReviews() throws Exception {
		// Given
		ReviewAdminResponse mockResponse = new ReviewAdminResponse(
			1L,
			"testContent",
			5,
			1L,
			"testCatalogName",
			LocalDateTime.now(),
			LocalDateTime.now()
		);
		List<ReviewAdminResponse> content = Collections.singletonList(mockResponse);
		CommonPageResponse<ReviewAdminResponse> commonPageResponse = CommonPageResponse.from(
			new PageImpl<>(content)
		);

		when(reviewAdminService.findReviews(any(ReviewAdminSearchRequest.class), any(Pageable.class)))
			.thenReturn(commonPageResponse);

		// When & Then
		mockMvc.perform(get("/admin/reviews")
				.param("keyword", "test")
				.param("isDeleted", "false")
				.param("page", "1")
				.param("size", "10"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("리뷰 수정 성공")
	void success_updateReview() throws Exception {
		// Given
		Long reviewId = 1L;
		ReviewAdminUpdateRequest request = new ReviewAdminUpdateRequest("updated content", 4);
		doNothing().when(reviewAdminService).updateReview(eq(reviewId), any(ReviewAdminUpdateRequest.class));

		// When & Then
		mockMvc.perform(patch("/admin/reviews/{reviewId}", reviewId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void success_deleteReview() throws Exception {
		// Given
		Long reviewId = 1L;
		doNothing().when(reviewAdminService).deleteReview(reviewId);

		// When & Then
		mockMvc.perform(delete("/admin/reviews/{reviewId}", reviewId))
			.andExpect(status().isNoContent());
	}
}
