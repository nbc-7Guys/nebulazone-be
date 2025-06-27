package nbc.chillguys.nebulazone.domain.review.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

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
import org.springframework.test.util.ReflectionTestUtils;

import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminUpdateRequest;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminInfo;
import nbc.chillguys.nebulazone.domain.review.dto.ReviewAdminSearchQueryCommand;
import nbc.chillguys.nebulazone.domain.review.entity.Review;
import nbc.chillguys.nebulazone.domain.review.exception.ReviewException;
import nbc.chillguys.nebulazone.domain.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewAdminDomainServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private ReviewAdminDomainService reviewAdminDomainService;

	@Test
	@DisplayName("findReviews - Success")
	void findReviews_Success() {
		// Given
		ReviewAdminSearchQueryCommand command = new ReviewAdminSearchQueryCommand("keyword");
		Pageable pageable = PageRequest.of(0, 10);

		// Catalog Mock 객체 준비
		Catalog mockCatalog = Catalog.builder()
			.name("testCatalog")
			.build();
		ReflectionTestUtils.setField(mockCatalog, "id", 100L); // 예시 id

		Review mockReview = Review.builder()
			.content("testContent")
			.star(5)
			.catalog(mockCatalog) // catalog mock 객체 주입!
			.build();
		ReflectionTestUtils.setField(mockReview, "id", 1L);

		Page<Review> mockReviewPage = new PageImpl<>(Collections.singletonList(mockReview), pageable, 1);

		when(reviewRepository.searchReviews(any(ReviewAdminSearchQueryCommand.class), any(Pageable.class)))
			.thenReturn(mockReviewPage);

		// When
		Page<ReviewAdminInfo> result = reviewAdminDomainService.findReviews(command, pageable);

		// Then
		assertEquals(1, result.getContent().size());
		assertEquals(mockReview.getId(), result.getContent().getFirst().reviewId());
		verify(reviewRepository).searchReviews(any(ReviewAdminSearchQueryCommand.class), any(Pageable.class));
	}

	@Test
	@DisplayName("updateReview - Success")
	void updateReview_Success() {
		// Given
		Long reviewId = 1L;
		ReviewAdminUpdateRequest request = new ReviewAdminUpdateRequest("updated content", 4);
		Review mockReview = Review.builder()
			.content("old content")
			.star(3)
			.catalog(null)
			.build();
		ReflectionTestUtils.setField(mockReview, "id", reviewId);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

		// When
		reviewAdminDomainService.updateReview(reviewId, request);

		// Then
		assertEquals("updated content", mockReview.getContent());
		assertEquals(4, mockReview.getStar());
		verify(reviewRepository).findById(reviewId);
	}

	@Test
	@DisplayName("updateReview - Review Not Found")
	void updateReview_NotFound() {
		// Given
		Long reviewId = 1L;
		ReviewAdminUpdateRequest request = new ReviewAdminUpdateRequest("updated content", 4);
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(ReviewException.class, () -> reviewAdminDomainService.updateReview(reviewId, request));
		verify(reviewRepository).findById(reviewId);
	}

	@Test
	@DisplayName("deleteReview - Success")
	void deleteReview_Success() {
		// Given
		Long reviewId = 1L;
		Review mockReview = Review.builder().build();
		ReflectionTestUtils.setField(mockReview, "id", reviewId);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));
		doNothing().when(reviewRepository).delete(mockReview);

		// When
		reviewAdminDomainService.deleteReview(reviewId);

		// Then
		verify(reviewRepository).findById(reviewId);
		verify(reviewRepository).delete(mockReview);
	}

	@Test
	@DisplayName("findById - Success")
	void findById_Success() {
		// Given
		Long reviewId = 1L;
		Review mockReview = Review.builder().build();
		ReflectionTestUtils.setField(mockReview, "id", reviewId);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(mockReview));

		// When
		Review result = reviewAdminDomainService.findById(reviewId);

		// Then
		assertEquals(reviewId, result.getId());
		verify(reviewRepository).findById(reviewId);
	}

	@Test
	@DisplayName("findById - Review Not Found")
	void findById_NotFound() {
		// Given
		Long reviewId = 1L;
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(ReviewException.class, () -> reviewAdminDomainService.findById(reviewId));
		verify(reviewRepository).findById(reviewId);
	}
}
