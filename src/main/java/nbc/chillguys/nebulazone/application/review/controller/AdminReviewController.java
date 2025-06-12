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
import nbc.chillguys.nebulazone.application.review.dto.request.AdminReviewSearchRequest;
import nbc.chillguys.nebulazone.application.review.dto.request.AdminReviewUpdateRequest;
import nbc.chillguys.nebulazone.application.review.dto.response.AdminReviewResponse;
import nbc.chillguys.nebulazone.application.review.service.AdminReviewService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
	private final AdminReviewService adminReviewService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AdminReviewResponse>> findReviews(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "isDeleted", required = false) Boolean isDeleted,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminReviewSearchRequest request = new AdminReviewSearchRequest(keyword, isDeleted, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminReviewResponse> response = adminReviewService.findReviews(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{reviewId}")
	public ResponseEntity<Void> updateReview(
		@PathVariable Long reviewId,
		@RequestBody AdminReviewUpdateRequest request
	) {
		adminReviewService.updateReview(reviewId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
		adminReviewService.deleteReview(reviewId);
		return ResponseEntity.noContent().build();
	}

}
