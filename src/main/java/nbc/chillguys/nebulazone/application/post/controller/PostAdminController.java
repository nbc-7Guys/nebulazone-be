package nbc.chillguys.nebulazone.application.post.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.PostAdminSearchRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.PostAdminUpdateTypeRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.UpdateImagesPostRequest;
import nbc.chillguys.nebulazone.application.post.dto.request.UpdatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.DeletePostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.GetPostResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.PostAdminResponse;
import nbc.chillguys.nebulazone.application.post.dto.response.UpdatePostResponse;
import nbc.chillguys.nebulazone.application.post.service.PostAdminService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class PostAdminController {
	private final PostAdminService postAdminService;

	@GetMapping
	public ResponseEntity<CommonPageResponse<PostAdminResponse>> findPosts(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "type", required = false) PostType type,
		@RequestParam(value = "includeDeleted", required = false, defaultValue = "false") boolean includeDeleted,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		PostAdminSearchRequest request = new PostAdminSearchRequest(keyword, type, includeDeleted, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<PostAdminResponse> response = postAdminService.findPosts(request, pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{postId}")
	public ResponseEntity<GetPostResponse> getAdminPost(@PathVariable("postId") Long postId) {
		GetPostResponse response = postAdminService.getAdminPost(postId);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/{postId}")
	public ResponseEntity<UpdatePostResponse> updateAdminPost(
		@PathVariable("postId") Long postId,
		@Valid @RequestPart("post") UpdatePostRequest request) {
		UpdatePostResponse response = postAdminService.updateAdminPost(postId, request);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{postId}/type")
	public ResponseEntity<Void> updatePostType(
		@PathVariable Long postId,
		@RequestBody PostAdminUpdateTypeRequest request
	) {
		postAdminService.updatePostType(postId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<DeletePostResponse> deletePost(
		@PathVariable("postId") Long postId
	) {
		DeletePostResponse response = postAdminService.deleteAdminPost(postId);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{postId}/restore")
	public ResponseEntity<Void> restorePost(@PathVariable Long postId) {
		postAdminService.restorePost(postId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping(value = "/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<GetPostResponse> updatePostImages(
		@ImageFile @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
		@PathVariable("postId") Long postId,
		@Valid @RequestPart("post") UpdateImagesPostRequest request
	) {

		GetPostResponse response = postAdminService.updatePostImages(postId, imageFiles,
			request.remainImageUrls());

		return ResponseEntity.ok(response);
	}
}
