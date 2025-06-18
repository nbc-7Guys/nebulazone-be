package nbc.chillguys.nebulazone.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import nbc.chillguys.nebulazone.application.user.dto.request.SignUpUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.UpdateUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.request.WithdrawUserRequest;
import nbc.chillguys.nebulazone.application.user.dto.response.UserResponse;
import nbc.chillguys.nebulazone.application.user.dto.response.WithdrawUserResponse;
import nbc.chillguys.nebulazone.domain.user.dto.UserSignUpCommand;
import nbc.chillguys.nebulazone.domain.user.dto.UserUpdateCommand;
import nbc.chillguys.nebulazone.domain.user.entity.User;
import nbc.chillguys.nebulazone.domain.user.service.UserDomainService;
import nbc.chillguys.nebulazone.infra.aws.s3.S3Service;
import nbc.chillguys.nebulazone.infra.redis.service.UserCacheService;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserDomainService userDomainService;
	private final S3Service s3Service;
	private final UserCacheService userCacheService;

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

	public UserResponse updateUserNicknameOrPassword(UpdateUserRequest updateUserRequest, User loggedInUser) {
		User user = userDomainService.updateUserNicknameOrPassword(
			UserUpdateCommand.of(updateUserRequest, loggedInUser)
		);

		userCacheService.deleteUserById(user.getId());

		return UserResponse.from(user);
	}

	@Transactional
	public UserResponse updateUserProfileImage(MultipartFile profileImage, User user) {
		if (user.getProfileImage() != null) {
			s3Service.generateDeleteUrlAndDeleteFile(user.getProfileImage());
		}

		String profileImageUrl = s3Service.generateUploadUrlAndUploadFile(profileImage);

		userDomainService.updateUserProfileImage(profileImageUrl, user);

		userCacheService.deleteUserById(user.getId());

		return UserResponse.from(user);
	}

	public WithdrawUserResponse withdrawUser(WithdrawUserRequest withdrawUserRequest, User user) {
		userDomainService.validPassword(withdrawUserRequest.password(), user.getPassword());

		userDomainService.withdrawUser(user);

		userCacheService.deleteUserById(user.getId());

		return WithdrawUserResponse.from(user.getId());
	}
}
