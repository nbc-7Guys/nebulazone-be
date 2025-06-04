package nbc.chillguys.nebulazone.application.review.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.List;

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
import org.springframework.test.context.ActiveProfiles;

import nbc.chillguys.nebulazone.application.review.dto.response.ReviewResponse;
import nbc.chillguys.nebulazone.domain.catalog.entity.Catalog;
import nbc.chillguys.nebulazone.domain.catalog.entity.CatalogType;
import nbc.chillguys.nebulazone.domain.review.entity.Review;
import nbc.chillguys.nebulazone.domain.review.service.ReviewDomainService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("리뷰 서비스 테스트")
class ReviewServiceTest {
	@Mock
	private ReviewDomainService reviewDomainService;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰 서비스 페이징 조회 테스트")
	void success_findReview() {
		// given
		Long catalogId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		Catalog catalog1 = new Catalog("이름", "설명", CatalogType.CPU);
		Catalog catalog2 = new Catalog("이름2", "설명2", CatalogType.GPU);

		List<Review> reviewList = List.of(
			Review.builder()
				.content("내용1")
				.star(5)
				.catalog(catalog1)
				.build(),
			Review.builder()
				.content("내용2")
				.star(3)
				.catalog(catalog2)
				.build()
		);

		PageImpl<Review> reviewPage = new PageImpl<>(reviewList, pageable, reviewList.size());
		when(reviewDomainService.findReviews(catalogId, pageable)).thenReturn(reviewPage);

		// when
		Page<ReviewResponse> result = reviewService.findReviews(catalogId, pageable);

		// then
		assertThat(result.getContent().get(0).content()).isEqualTo("내용1");
		assertThat(result.getContent().get(1).star()).isEqualTo(3);
		verify(reviewDomainService).findReviews(catalogId, pageable);
	}
}
