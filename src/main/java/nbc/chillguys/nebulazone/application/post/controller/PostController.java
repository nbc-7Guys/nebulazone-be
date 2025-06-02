package nbc.chillguys.nebulazone.application.post.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.post.dto.request.CreatePostRequest;
import nbc.chillguys.nebulazone.application.post.dto.response.CreatePostResponse;
import nbc.chillguys.nebulazone.application.post.service.PostService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CreatePostResponse> createPost(
		@AuthenticationPrincipal AuthUser authUser,
		@Valid @RequestPart("post") CreatePostRequest request,
		@RequestPart("images") List<MultipartFile> multipartFiles) {

		CreatePostResponse postResponse = postService.createPost(authUser, request, multipartFiles);

		return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
	}

}
