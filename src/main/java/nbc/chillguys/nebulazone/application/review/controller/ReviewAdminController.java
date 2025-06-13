package nbc.chillguys.nebulazone.application.review.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminSearchRequest;
import nbc.chillguys.nebulazone.application.review.dto.request.ReviewAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.review.dto.response.ReviewAdminResponse;
import nbc.chillguys.nebulazone.application.review.service.ReviewAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class ReviewAdminController {
	private final ReviewAdminService reviewAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<ReviewAdminResponse>> findReviews(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "isDeleted", required = false) Boolean isDeleted,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		ReviewAdminSearchRequest request = new ReviewAdminSearchRequest(keyword, isDeleted, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<ReviewAdminResponse> response = reviewAdminService.findReviews(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}")
	public ResponseEntity<Void> updateReview(
		@PathVariable Long reviewId,
		@RequestBody ReviewAdminUpdateRequest request
	) {
		reviewAdminService.updateReview(reviewId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
		reviewAdminService.deleteReview(reviewId);
		return ResponseEntity.noContent().build();
	}

}
