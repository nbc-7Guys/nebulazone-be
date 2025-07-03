package nbc.chillguys.nebulazone.application.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.AddAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.DeleteAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateAddressUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.application.user.service.UserService;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;
import nbc.chillguys.nebulazone.domain.user.entity.User;

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

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal User user) {
		UserResponse response = UserResponse.from(user);

		return ResponseEntity.ok(response);
	}

	@PatchMapping
	public ResponseEntity<UserResponse> updateUserNicknameOrPassword(
		@Valid @RequestBody UpdateUserRequest updateUserRequest,
		@AuthenticationPrincipal User user
	) {
		UserResponse response = userService.updateUserNicknameOrPassword(updateUserRequest, user);

		return ResponseEntity.ok(response);
	}

	@PutMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<UserResponse> updateUserProfileImage(
		@ImageFile @RequestPart("profileImage") MultipartFile profileImage,
		@AuthenticationPrincipal User user
	) {
		UserResponse response = userService.updateUserProfileImage(profileImage, user);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public ResponseEntity<WithdrawUserResponse> withdrawUser(
		@Valid @RequestBody WithdrawUserRequest withdrawUserRequest,
		@AuthenticationPrincipal User user
	) {
		WithdrawUserResponse response = userService.withdrawUser(withdrawUserRequest, user);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/me/address")
	public ResponseEntity<UserResponse> addAddress(
		@Valid @RequestBody AddAddressUserRequest addAddressUserRequest,
		@AuthenticationPrincipal User user
	) {
		UserResponse response = userService.addAddress(addAddressUserRequest, user);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(response);
	}

	@PutMapping("/me/address")
	public ResponseEntity<UserResponse> updateAddress(
		@Valid @RequestBody UpdateAddressUserRequest updateAddressUserRequest,
		@AuthenticationPrincipal User user
	) {
		UserResponse response = userService.updateAddress(updateAddressUserRequest, user);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/me/address")
	public ResponseEntity<UserResponse> deleteAddress(
		@Valid @RequestBody DeleteAddressUserRequest deleteAddressUserRequest,
		@AuthenticationPrincipal User user
	) {
		UserResponse response = userService.deleteAddress(deleteAddressUserRequest, user);

		return ResponseEntity.ok(response);
	}
}
