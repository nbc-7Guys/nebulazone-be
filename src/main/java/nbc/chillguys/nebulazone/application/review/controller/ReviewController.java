package nbc.chillguys.nebulazone.application.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewResponse;
import nbc.chillguys.nebulazone.application.review.service.ReviewService;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	@GetMapping("/{catalogId}/reviews")
	public ResponseEntity<Page<ReviewResponse>> findReviews(
		@PathVariable Long catalogId,
		@PageableDefault(size = 10)
		Pageable pageable
	) {
		Page<ReviewResponse> response = reviewService.findReviews(catalogId, pageable);
		return ResponseEntity.ok(response);
	}
}
