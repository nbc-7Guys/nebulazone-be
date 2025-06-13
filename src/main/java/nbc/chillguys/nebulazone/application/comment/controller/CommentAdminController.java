package nbc.chillguys.nebulazone.application.comment.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.CommentAdminSearchRequest;
import nbc.chillguys.nebulazone.application.comment.dto.request.CommentAdminUpdateRequest;
import nbc.chillguys.nebulazone.application.comment.dto.response.CommentAdminResponse;
import nbc.chillguys.nebulazone.application.comment.service.CommentAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {
	private final CommentAdminService commentAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<CommentAdminResponse>> findComments(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "deleted", required = false) Boolean deleted,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		CommentAdminSearchRequest request = new CommentAdminSearchRequest(keyword, deleted, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<CommentAdminResponse> response = commentAdminService.findComments(request, pageable);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{commentId}")
	public ResponseEntity<Void> updateComment(
		@PathVariable Long commentId,
		@RequestBody CommentAdminUpdateRequest request
	) {
		commentAdminService.updateComment(commentId, request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
		commentAdminService.deleteComment(commentId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{commentId}/restore")
	public ResponseEntity<Void> restoreComment(@PathVariable Long commentId) {
		commentAdminService.restoreComment(commentId);
		return ResponseEntity.noContent().build();
	}

}
