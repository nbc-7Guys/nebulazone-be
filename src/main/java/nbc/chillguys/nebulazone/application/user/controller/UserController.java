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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.application.user.service.UserService;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<UserResponse> signup(
		@Valid @RequestBody SignUpUserRequest signUpUserRequest
	) {
		UserResponse responseDto = userService.signUp(signUpUserRequest);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(responseDto);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<UserResponse> getUser(@PathVariable("userId") Long userId) {
		UserResponse response = userService.getUser(userId);

		return ResponseEntity.ok(response);
	}

	@PatchMapping
	public ResponseEntity<UserResponse> updateUserNicknameOrPassword(
		@Valid @RequestBody UpdateUserRequest updateUserRequest,
		@AuthenticationPrincipal AuthUser authUser
	) {
		UserResponse response = userService.updateUserNicknameOrPassword(updateUserRequest, authUser);

		return ResponseEntity.ok(response);
	}

	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponse> updateUserProfileImage(
		@ImageFile @RequestPart("profileImage") MultipartFile profileImage,
		@AuthenticationPrincipal AuthUser authUser
	) {
		UserResponse response = userService.updateUserProfileImage(profileImage, authUser);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public ResponseEntity<WithdrawUserResponse> withdrawUser(
		@Valid @RequestBody WithdrawUserRequest withdrawUserRequest,
		@AuthenticationPrincipal AuthUser authUser
	) {
		WithdrawUserResponse response = userService.withdrawUser(withdrawUserRequest, authUser);

		return ResponseEntity.ok(response);
	}
}
