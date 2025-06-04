package nbc.chillguys.nebulazone.application.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.service.UserService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@Operation(
		summary = "회원가입",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
				mediaType = "multipart/form-data",
				encoding = {
					@Encoding(name = "signUpUserRequest", contentType = "application/json")
				}
			)
		)
	)
	@PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponse> signup(
		@Valid @RequestPart("signUpUserRequest") SignUpUserRequest signUpUserRequest,
		@ImageFile @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		UserResponse responseDto = userService.signUp(signUpUserRequest, profileImage);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(responseDto);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUser(@PathVariable("userId") Long userId) {
		UserResponse response = userService.getUser(userId);

		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "유저수정",
		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
			content = @Content(
				mediaType = "multipart/form-data",
				encoding = {
					@Encoding(name = "updateUserRequest", contentType = "application/json")
				}
			)
		)
	)
	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponse> updateUser(
		@Valid @RequestPart(value = "updateUserRequest", required = false) UpdateUserRequest updateUserRequest,
		@ImageFile @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
		@AuthenticationPrincipal AuthUser authUser
	) {
		UserResponse response = userService.updateUser(updateUserRequest, profileImage, authUser);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public ResponseEntity<Long> withdrawUser(
		@Valid @RequestBody WithdrawUserRequest withdrawUserRequest,
		@AuthenticationPrincipal AuthUser authUser
	) {
		Long withdrawnUserId = userService.withdrawUser(withdrawUserRequest, authUser);

		return ResponseEntity.ok(withdrawnUserId);
	}
}
