package nbc.chillguys.nebulazone.application.comment.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.AdminCommentSearchRequest;
import nbc.chillguys.nebulazone.application.comment.dto.response.AdminCommentResponse;
import nbc.chillguys.nebulazone.application.comment.service.AdminCommentService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
	private final AdminCommentService adminCommentService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<AdminCommentResponse>> findComments(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "deleted", required = false) Boolean deleted,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminCommentSearchRequest request = new AdminCommentSearchRequest(keyword, deleted, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminCommentResponse> response = adminCommentService.findComments(request, pageable);
		return ResponseEntity.ok(response);
	}
}
