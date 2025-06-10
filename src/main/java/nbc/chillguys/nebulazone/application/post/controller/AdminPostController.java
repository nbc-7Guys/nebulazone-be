package nbc.chillguys.nebulazone.application.post.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.AdminPostSearchRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.AdminPostResponse;
import nbc.chillguys.nebulazone.application.post.service.AdminPostService;
import nbc.chillguys.nebulazone.common.response.CommonPageResponse;
import nbc.chillguys.nebulazone.domain.post.entity.PostType;

@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {
	private final AdminPostService adminPostService;

	@GetMapping()
	public ResponseEntity<CommonPageResponse<AdminPostResponse>> findPosts(
		@RequestParam(value = "keyword", required = false) String keyword,
		@RequestParam(value = "type", required = false) PostType type,
		@RequestParam(value = "includeDeleted", required = false, defaultValue = "false") boolean includeDeleted,
		@RequestParam(value = "page", defaultValue = "1") int page,
		@RequestParam(value = "size", defaultValue = "10") int size
	) {
		AdminPostSearchRequest request = new AdminPostSearchRequest(keyword, type, includeDeleted, page, size);
		Pageable pageable = PageRequest.of(page - 1, size);
		CommonPageResponse<AdminPostResponse> response = adminPostService.findPosts(request, pageable);
		return ResponseEntity.ok(response);
	}

}
