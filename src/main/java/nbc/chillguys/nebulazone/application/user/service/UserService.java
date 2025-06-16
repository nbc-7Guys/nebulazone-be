package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.domain.auth.vo.AuthUser;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.dto.UserUpdateCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserDomainService userDomainService;
	private final S3Service s3Service;

	public UserResponse signUp(SignUpUserRequest signUpUserRequest) {
		userDomainService.validEmail(signUpUserRequest.email());

		userDomainService.validNickname(signUpUserRequest.nickname());

		userDomainService.validPhone(signUpUserRequest.phone());

		User user = userDomainService.createUser(UserSignUpCommand.from(signUpUserRequest));

		return UserResponse.from(user);
	}

	public UserResponse getUser(Long userId) {
		User targetUser = userDomainService.findActiveUserById(userId);

		return UserResponse.from(targetUser);
	}

	public UserResponse updateUserNicknameOrPassword(UpdateUserRequest updateUserRequest, AuthUser authUser) {
		User user = userDomainService.updateUserNicknameOrPassword(
			UserUpdateCommand.of(updateUserRequest, authUser.getId())
		);

		return UserResponse.from(user);
	}

	@Transactional
	public UserResponse updateUserProfileImage(MultipartFile profileImage, AuthUser authUser) {
		User user = userDomainService.findActiveUserById(authUser.getId());

		if (user.getProfileImage() != null) {
			s3Service.generateDeleteUrlAndDeleteFile(user.getProfileImage());
		}

		String profileImageUrl = s3Service.generateUploadUrlAndUploadFile(profileImage);
		log.info("profileImageUrl: {}", profileImageUrl);

		userDomainService.updateUserProfileImage(profileImageUrl, user);

		return UserResponse.from(user);
	}

	public WithdrawUserResponse withdrawUser(WithdrawUserRequest withdrawUserRequest, AuthUser authUser) {
		User user = userDomainService.findActiveUserById(authUser.getId());

		userDomainService.validPassword(withdrawUserRequest.password(), user.getPassword());

		userDomainService.withdrawUser(user);

		return WithdrawUserResponse.from(user.getId());
	}
}
