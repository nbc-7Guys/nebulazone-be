package nbc.chillguys.nebulazone.application.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.service.UserService;
import nbc.chillguys.nebulazone.domain.common.validator.image.ImageFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@Operation(
		summary = "회원가입",
		requestBody = @RequestBody(
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
}
