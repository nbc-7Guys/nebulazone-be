package nbc.chillguys.nebulazone.application.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.comment.dto.request.CreateCommentRequest;
import nbc.chillguys.nebulazone.application.comment.dto.request.UpdateCommentRequest;
import nbc.chillguys.nebulazone.application.comment.dto.response.CommentDetailResponse;
import nbc.chillguys.nebulazone.application.comment.dto.response.CommentResponse;
import nbc.chillguys.nebulazone.application.comment.dto.response.DeleteCommentResponse;
import nbc.chillguys.nebulazone.application.comment.service.CommentService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<CommentResponse> createComment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("postId") Long postId,
		@Valid @RequestBody CreateCommentRequest request
	) {
		CommentResponse response = commentService.createComment(authUser.getId(), postId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<CommonPageResponse<CommentDetailResponse>> findComments(
		@PathVariable("postId") Long postId,
		@RequestParam(value = "page", defaultValue = "1", required = false) int page,
		@RequestParam(value = "size", defaultValue = "20", required = false) int size
	) {
		CommonPageResponse<CommentDetailResponse> response = commentService.findComments(postId, page, size);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{commentId}")
	public ResponseEntity<CommentResponse> updateComment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("postId") Long postId,
		@PathVariable("commentId") Long commentId,
		@Valid @RequestBody UpdateCommentRequest request
	) {
		CommentResponse response = commentService.updateComment(authUser.getId(), postId, commentId, request);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{commentId}")
	public ResponseEntity<DeleteCommentResponse> deleteComment(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable("postId") Long postId,
		@PathVariable("commentId") Long commentId
	) {
		DeleteCommentResponse response = commentService.deleteComment(authUser.getId(), postId, commentId);

		return ResponseEntity.ok(response);
	}
}
