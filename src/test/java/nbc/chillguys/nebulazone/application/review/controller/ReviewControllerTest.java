package nbc.chillguys.nebulazone.application.review.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import nbc.chillguys.nebulazone.application.review.dto.response.ReviewResponse;
import nbc.chillguys.nebulazone.application.review.service.ReviewService;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("리뷰 컨트롤러 테스트")
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰 페이징 조회 성공")
	void success_findReviews() throws Exception {
		// given
		Long catalogId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		List<ReviewResponse> reviews = List.of(
			new ReviewResponse(1L, "좋아요!", 5, LocalDateTime.now()),
			new ReviewResponse(2L, "별로예요", 2, LocalDateTime.now())
		);

		Page<ReviewResponse> reviewPage = new PageImpl<>(reviews, pageable, reviews.size());

		when(reviewService.findReviews(catalogId, pageable)).thenReturn(reviewPage);

		// when & then
		mockMvc.perform(get("/catalog/{catalogId}/reviews", catalogId)
				.param("page", "0")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.content[0].id").value(1))
			.andExpect(jsonPath("$.content[0].content").value("좋아요!"))
			.andExpect(jsonPath("$.content[0].star").value(5))
			.andExpect(jsonPath("$.content[1].id").value(2))
			.andExpect(jsonPath("$.content[1].content").value("별로예요"))
			.andExpect(jsonPath("$.content[1].star").value(2))
			.andExpect(jsonPath("$.totalElements").value(2))
			.andExpect(jsonPath("$.totalPages").value(1))
			.andExpect(jsonPath("$.size").value(10))
			.andExpect(jsonPath("$.number").value(0));
	}
}
