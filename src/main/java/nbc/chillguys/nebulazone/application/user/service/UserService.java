package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserDomainService userDomainService;
	private final S3Service s3Service;

	@Transactional
	public UserResponse signUp(SignUpUserRequest signUpUserRequest, MultipartFile profileImage) {
		userDomainService.validEmail(signUpUserRequest.email());

		userDomainService.validNickname(signUpUserRequest.nickname());

		String profileImageUrl = s3Service.generateUploadUrlAndUploadFile(profileImage);

		User user = userDomainService.createUser(UserSignUpCommand.of(signUpUserRequest, profileImageUrl));

		return UserResponse.from(user);
	}
}
