package nbc.chillguys.nebulazone.application.review.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminSearchRequest;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewAdminResponse;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminInfo;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.service.ReviewAdminDomainService;

@ExtendWith(MockitoExtension.class)
class ReviewAdminServiceTest {

	@Mock
	private ReviewAdminDomainService reviewAdminDomainService;

	@InjectMocks
	private ReviewAdminService reviewAdminService;

	@Test
	@DisplayName("리뷰 조회 성공")
	void success_findReviews() {
		// Given
		ReviewAdminSearchRequest request = new ReviewAdminSearchRequest("keyword", false, 1, 10);
		Pageable pageable = PageRequest.of(0, 10);

		ReviewAdminInfo mockInfo = new ReviewAdminInfo(
			1L, "testContent", 5, 100L, "testCatalog", LocalDateTime.now(), LocalDateTime.now()
		);
		Page<ReviewAdminInfo> mockInfoPage = new PageImpl<>(Collections.singletonList(mockInfo), pageable, 1);

		when(reviewAdminDomainService.findReviews(any(ReviewAdminSearchQueryCommand.class), any(Pageable.class)))
			.thenReturn(mockInfoPage);

		// When
		CommonPageResponse<ReviewAdminResponse> result = reviewAdminService.findReviews(request, pageable);

		// Then
		assertEquals(1, result.content().size());
		assertEquals(mockInfo.reviewId(), result.content().get(0).reviewId());
		verify(reviewAdminDomainService).findReviews(any(ReviewAdminSearchQueryCommand.class), any(Pageable.class));
	}

	@Test
	@DisplayName("리뷰 수정 성공")
	void success_updateReview() {
		// Given
		Long reviewId = 1L;
		ReviewAdminUpdateRequest request = new ReviewAdminUpdateRequest("updated content", 4);
		doNothing().when(reviewAdminDomainService).updateReview(reviewId, request);

		// When
		reviewAdminService.updateReview(reviewId, request);

		// Then
		verify(reviewAdminDomainService).updateReview(reviewId, request);
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void success_deleteReview() {
		// Given
		Long reviewId = 1L;
		doNothing().when(reviewAdminDomainService).deleteReview(reviewId);

		// When
		reviewAdminService.deleteReview(reviewId);

		// Then
		verify(reviewAdminDomainService).deleteReview(reviewId);
	}
}
